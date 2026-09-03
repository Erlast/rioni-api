package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.AuthRequest;
import com.rioni.lk.api.dto.AuthResponse;
import com.rioni.lk.api.dto.ChangePasswordRequest;
import com.rioni.lk.api.dto.CheckContactRequest;
import com.rioni.lk.api.dto.CheckContactResponse;
import com.rioni.lk.api.dto.RecoverSmsRequest;
import com.rioni.lk.api.dto.RegistrationRequest;
import com.rioni.lk.api.dto.RegistrationSmsRequest;
import com.rioni.lk.api.dto.SmsCodeRequest;
import com.rioni.lk.api.dto.SmsSendResponse;
import com.rioni.lk.api.exception.AuthException;
import com.rioni.lk.api.exception.DataNotFoundException;
import com.rioni.lk.api.exception.RegistrationException;
import com.rioni.lk.api.exception.SmsCodeAttemptsExceededException;
import com.rioni.lk.api.exception.SmsCodeInvalidException;
import com.rioni.lk.api.exception.SmsCodeNotFoundException;
import com.rioni.lk.api.exception.SmsCodeSendException;
import com.rioni.lk.api.model.Account;
import com.rioni.lk.api.model.Profile;
import com.rioni.lk.api.model.ProfileAddress;
import com.rioni.lk.api.model.ProfileContact;
import com.rioni.lk.api.model.ProfilePassword;
import com.rioni.lk.api.model.SmsCode;
import com.rioni.lk.api.model.Subaccount;
import com.rioni.lk.api.repository.AccountRepository;
import com.rioni.lk.api.repository.ProfileAddressRepository;
import com.rioni.lk.api.repository.ProfileContactRepository;
import com.rioni.lk.api.repository.ProfilePasswordRepository;
import com.rioni.lk.api.repository.ProfileRepository;
import com.rioni.lk.api.repository.SmsCodeRepository;
import com.rioni.lk.api.repository.SubaccountRepository;
import com.rioni.lk.api.service.AuthService;
import com.rioni.lk.api.util.PhoneUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEV_SMS_CODE = "111111";
    private static final String REGISTRATION_SMS_CODE = "333333";
    private static final int SMS_CODE_VALIDITY_MINUTES = 5;
    private static final int SMS_CODE_BLOCK_DURATION_MINUTES = 30;
    private static final int SMS_CODE_MAX_ATTEMPTS = 3;

    private final ProfilePasswordRepository profilePasswordRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileAddressRepository profileAddressRepository;
    private final SmsCodeRepository smsCodeRepository;
    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final SubaccountRepository subaccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public AuthServiceImpl(
            ProfilePasswordRepository profilePasswordRepository,
            ProfileContactRepository profileContactRepository,
            ProfileAddressRepository profileAddressRepository,
            SmsCodeRepository smsCodeRepository,
            ProfileRepository profileRepository,
            AccountRepository accountRepository,
            SubaccountRepository subaccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.profilePasswordRepository = profilePasswordRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileAddressRepository = profileAddressRepository;
        this.smsCodeRepository = smsCodeRepository;
        this.profileRepository = profileRepository;
        this.accountRepository = accountRepository;
        this.subaccountRepository = subaccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    @Transactional
    public SmsSendResponse authenticate(AuthRequest request) {
        Optional<ProfilePassword> profilePasswordOpt = profilePasswordRepository.findByProfileLogin(request.getLogin());
        
        if (profilePasswordOpt.isEmpty()) {
            throw new AuthException("Wrong login or password");
        }
        
        ProfilePassword profilePassword = profilePasswordOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), profilePassword.getPasswordHash())) {
            throw new AuthException("Wrong login or password");
        }
        
        int profileId = profilePassword.getProfile().getId();
        
        Optional<ProfileContact> phoneContact = profileContactRepository
                .findTopByProfileIdAndContactTypeOrderByIsMainDesc(profileId, "phone");
        
        if (phoneContact.isEmpty() || phoneContact.get().getValue() == null || phoneContact.get().getValue().isEmpty()) {
            throw new RuntimeException("Phone number not specified in profile");
        }
        
        String phone = phoneContact.get().getValue();
        String normalizedPhone = PhoneUtils.normalize(phone);
        String maskedPhone = PhoneUtils.mask(normalizedPhone);

        // Check if the latest unused authorization SMS code for this phone
        // has exceeded the attempt limit and was created within the last 30 minutes
        smsCodeRepository
                .findTopByPhoneAndPurposeAndIsUsedOrderByCreatedAtDesc(normalizedPhone, "authorization", false)
                .ifPresent(lastSmsCode -> {
                    LocalDateTime blockThreshold = LocalDateTime.now().minusMinutes(SMS_CODE_BLOCK_DURATION_MINUTES);
                    if (lastSmsCode.getAttemptedCount() > 3
                            && lastSmsCode.getCreatedAt().isAfter(blockThreshold)) {
                        long timeLeft = Duration.between(
                                LocalDateTime.now(),
                                lastSmsCode.getCreatedAt().plusMinutes(SMS_CODE_BLOCK_DURATION_MINUTES)
                        ).getSeconds();
                        throw new SmsCodeAttemptsExceededException("Attempts limit exceeded", Math.max(timeLeft, 0));
                    }
                });

        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(normalizedPhone);
        smsCode.setCode(DEV_SMS_CODE);
        smsCode.setCreatedAt(LocalDateTime.now());
        smsCode.setPurpose("authorization");
        
        SmsCode savedSmsCode = smsCodeRepository.save(smsCode);
        
        return new SmsSendResponse(
                "authorization",
                savedSmsCode.getId(),
                maskedPhone,
                profileId
        );
    }

    @Override
    @Transactional(noRollbackFor = {SmsCodeInvalidException.class, SmsCodeAttemptsExceededException.class})
    public AuthResponse checkSmsCode(SmsCodeRequest request) {
        Optional<SmsCode> smsCodeOpt = smsCodeRepository.findById(request.getSmsCodeId());
        
        if (smsCodeOpt.isEmpty()) {
            throw new SmsCodeNotFoundException("Sms code none exist");
        }
        
        SmsCode smsCode = smsCodeOpt.get();
        
        smsCode.setAttemptedCount(smsCode.getAttemptedCount() + 1);
        smsCodeRepository.save(smsCode);
        LocalDateTime blockThreshold = LocalDateTime.now().minusMinutes(SMS_CODE_BLOCK_DURATION_MINUTES);
        
        if (smsCode.getAttemptedCount() > 3) {
            long timeLeft = Duration.between(
                    LocalDateTime.now(),
                    smsCode.getCreatedAt().plusMinutes(SMS_CODE_BLOCK_DURATION_MINUTES)
            ).getSeconds();
            throw new SmsCodeAttemptsExceededException("Attempts limit exceeded", Math.max(timeLeft, 0));
        }
        
        if (smsCode.isUsed()) {
            throw new SmsCodeInvalidException("Wrong code");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = smsCode.getCreatedAt();
        LocalDateTime expiresAt = createdAt.plusMinutes(SMS_CODE_VALIDITY_MINUTES);
        
        if (now.isAfter(expiresAt)) {
            throw new SmsCodeInvalidException("Wrong code");
        }
        
        if (!smsCode.getCode().equals(request.getCode())) {
            throw new SmsCodeInvalidException("Wrong code");
        }
        
        smsCode.setUsed(true);
        smsCodeRepository.save(smsCode);

        String purpose = smsCode.getPurpose();

        // Recovery and registration purposes: the code is confirmed and marked
        // as used, but no tokens are issued. The response body is empty (200).
        // - Recovery: the client already knows the SMS code ID from the
        //   recover-sms response and passes it to the change password API.
        // - Registration: no profile exists for the phone yet.
        if ("recovery".equals(purpose) || "registration".equals(purpose)) {
            return null;
        }

        // Authorization purpose: issue access and refresh tokens.
        String phone = smsCode.getPhone();

        Optional<ProfileContact> phoneContact = profileContactRepository
                .findTopByValueAndContactTypeOrderByIsMainDesc(phone, "phone");

        if (phoneContact.isEmpty()) {
            throw new RuntimeException("Profile not found for this phone");
        }

        int profileId = phoneContact.get().getProfileId();

        String accessToken = generateAccessToken(profileId);
        String refreshToken = generateRefreshToken(profileId);
        
        return new AuthResponse(
                accessToken,
                "Bearer",
                refreshToken,
                accessTokenExpiration,
                refreshTokenExpiration
        );
    }

    @Override
    public AuthResponse refreshToken(int profileId) {
        String newAccessToken = generateAccessToken(profileId);
        String newRefreshToken = generateRefreshToken(profileId);

        return new AuthResponse(
                newAccessToken,
                "Bearer",
                newRefreshToken,
                accessTokenExpiration,
                refreshTokenExpiration
        );
    }

    private String generateAccessToken(int profileId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration * 1000);
        
        return Jwts.builder()
                .subject(String.valueOf(profileId))
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private String generateRefreshToken(int profileId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration * 1000);
        
        return Jwts.builder()
                .subject(String.valueOf(profileId))
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public void logout(int profileId) {
        // JWT tokens are stateless, so we just clear the security context on the client side
        // The client should discard the access and refresh tokens
        // If token invalidation is needed in the future, consider implementing a token blacklist
    }

    @Override
    public CheckContactResponse checkContact(CheckContactRequest request) {
        if ("login".equals(request.getType())) {
            Optional<Profile> profile = profileRepository.findByLogin(request.getValue());
            return new CheckContactResponse(profile.isPresent());
        }

        if (!"phone".equals(request.getType()) && !"email".equals(request.getType())) {
            return new CheckContactResponse(false);
        }

        String value = request.getValue();
        if ("phone".equals(request.getType())) {
            value = PhoneUtils.normalize(value);
        }

        Optional<ProfileContact> contact = profileContactRepository
                .findTopByValueAndContactTypeOrderByIsMainDesc(value, request.getType());

        return new CheckContactResponse(contact.isPresent());
    }

    @Override
    @Transactional
    public SmsSendResponse recoverSms(RecoverSmsRequest request) {
        if (!"phone".equals(request.getType()) && !"email".equals(request.getType())) {
            throw new DataNotFoundException("Data not found");
        }

        String value = request.getValue();
        String contactType = request.getType();

        if ("phone".equals(contactType)) {
            value = PhoneUtils.normalize(value);
        }

        Optional<ProfileContact> contactOpt = profileContactRepository
                .findTopByValueAndContactTypeOrderByIsMainDesc(value, contactType);

        if (contactOpt.isEmpty()) {
            throw new DataNotFoundException("Data not found");
        }

        int profileId = contactOpt.get().getProfileId();

        // Find the main phone contact for this profile
        Optional<ProfileContact> phoneContactOpt = profileContactRepository
                .findTopByProfileIdAndContactTypeOrderByIsMainDesc(profileId, "phone");

        if (phoneContactOpt.isEmpty() || phoneContactOpt.get().getValue() == null || phoneContactOpt.get().getValue().isEmpty()
                || !Boolean.TRUE.equals(phoneContactOpt.get().getIsMain())) {
            throw new DataNotFoundException("Data not found");
        }

        String phone = phoneContactOpt.get().getValue();
        String normalizedPhone = PhoneUtils.normalize(phone);

        // "Send" SMS — create a record in the database
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(normalizedPhone);
        smsCode.setCode("222222");
        smsCode.setCreatedAt(LocalDateTime.now());
        smsCode.setUsed(false);
        smsCode.setAttemptedCount(0);
        smsCode.setPurpose("recovery");

        SmsCode savedSmsCode = smsCodeRepository.save(smsCode);

        return new SmsSendResponse(
                "recovery",
                savedSmsCode.getId(),
                PhoneUtils.mask(normalizedPhone),
                0
        );
    }

    @Override
    @Transactional
    public SmsSendResponse sendRegistrationSms(RegistrationSmsRequest request) {
        String normalizedPhone = PhoneUtils.normalize(request.getPhone());

        if (normalizedPhone == null || normalizedPhone.isBlank()) {
            throw new SmsCodeSendException("Failed to send code");
        }

        // Check if the latest unused registration SMS code for this phone
        // has exceeded the attempt limit and was created within the last 30 minutes
        smsCodeRepository
                .findTopByPhoneAndPurposeAndIsUsedOrderByCreatedAtDesc(normalizedPhone, "registration", false)
                .ifPresent(lastSmsCode -> {
                    LocalDateTime blockThreshold = LocalDateTime.now().minusMinutes(SMS_CODE_BLOCK_DURATION_MINUTES);
                    if (lastSmsCode.getAttemptedCount() > SMS_CODE_MAX_ATTEMPTS
                            && lastSmsCode.getCreatedAt().isAfter(blockThreshold)) {
                        long timeLeft = Duration.between(
                                LocalDateTime.now(),
                                lastSmsCode.getCreatedAt().plusMinutes(SMS_CODE_BLOCK_DURATION_MINUTES)
                        ).getSeconds();
                        throw new SmsCodeAttemptsExceededException("Attempts limit exceeded", Math.max(timeLeft, 0));
                    }
                });

        // "Send" SMS — create a record in the database
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(normalizedPhone);
        smsCode.setCode(REGISTRATION_SMS_CODE);
        smsCode.setCreatedAt(LocalDateTime.now());
        smsCode.setUsed(false);
        smsCode.setAttemptedCount(0);
        smsCode.setPurpose("registration");

        try {
            SmsCode savedSmsCode = smsCodeRepository.save(smsCode);
            return new SmsSendResponse(
                    "registration",
                    savedSmsCode.getId(),
                    PhoneUtils.mask(normalizedPhone),
                    0
            );
        } catch (DataAccessException ex) {
            throw new SmsCodeSendException("Failed to send code");
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Optional<SmsCode> smsCodeOpt = smsCodeRepository.findById(request.getSmsCodeId());

        if (smsCodeOpt.isEmpty()) {
            throw new SmsCodeNotFoundException("Sms code none exist");
        }

        SmsCode smsCode = smsCodeOpt.get();

        // Identity must have been proven by a confirmed recovery SMS code
        // (check-sms returned the profile_id), so the old password may be omitted.
        if (!"recovery".equals(smsCode.getPurpose()) || !smsCode.isUsed()) {
            throw new SmsCodeInvalidException("Wrong code");
        }

        Optional<ProfileContact> phoneContact = profileContactRepository
                .findTopByValueAndContactTypeOrderByIsMainDesc(smsCode.getPhone(), "phone");

        if (phoneContact.isEmpty()) {
            throw new DataNotFoundException("Data not found");
        }

        int profileId = phoneContact.get().getProfileId();

        Optional<ProfilePassword> profilePasswordOpt = profilePasswordRepository.findByProfileId(profileId);

        if (profilePasswordOpt.isEmpty()) {
            throw new DataNotFoundException("Data not found");
        }

        ProfilePassword profilePassword = profilePasswordOpt.get();

        // If old password is provided, verify it. In the recovery flow the
        // identity is already proven by the confirmed recovery SMS code, so
        // the old password may be omitted.
        if (request.getOldPassword() != null && !request.getOldPassword().isBlank()) {
            if (!passwordEncoder.matches(request.getOldPassword(), profilePassword.getPasswordHash())) {
                throw new AuthException("Wrong old password");
            }
        }

        profilePassword.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        profilePasswordRepository.save(profilePassword);
    }

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        String login = resolveLogin(request);

        if (profileRepository.findByLogin(login).isPresent()) {
            throw new RegistrationException("Login already exists");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RegistrationException("Password is required");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RegistrationException("Passwords do not match");
        }

        Profile profile = new Profile();
        profile.setLogin(login);
        profile.setNickname(request.getName());
        fillGeneratedProfileFields(profile);

        Profile savedProfile = profileRepository.save(profile);
        int profileId = savedProfile.getId();

        ProfilePassword profilePassword = new ProfilePassword();
        profilePassword.setProfile(savedProfile);
        profilePassword.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        profilePasswordRepository.save(profilePassword);

        savePhoneContact(profileId, request.getPhone());
        saveEmailContact(profileId, request.getEmail());
        saveRegistrationAddress(profileId);

        String accountNumber = generateAccountNumber();
        Account account = new Account();
        account.setProfileId(profileId);
        account.setAccountNumber(accountNumber);
        account.setAccountType("BA");
        account.setAccountCurrencyId(1);
        Account savedAccount = accountRepository.save(account);

        for (String subaccountType : new String[]{"T", "W", "D"}) {
            Subaccount subaccount = new Subaccount();
            subaccount.setAccountId(savedAccount.getId());
            subaccount.setAccountNumber(accountNumber + "/" + subaccountType);
            subaccount.setSubaccountTypeCode(subaccountType);
            subaccountRepository.save(subaccount);
        }
    }

    private String resolveLogin(RegistrationRequest request) {
        if (request.getLogin() != null && !request.getLogin().isBlank()) {
            return request.getLogin().trim();
        }
        return request.getEmail();
    }

    /**
     * Generates placeholder values for the mandatory profile fields. The real
     * values will be provided by an external source in the future and are not
     * known at registration time yet.
     */
    private void fillGeneratedProfileFields(Profile profile) {
        String pending = "PENDING";
        profile.setName(pending);
        profile.setSurname(pending);
        profile.setPatronymic(pending);
        profile.setGender(pending);
        profile.setCitizenship(pending);
        profile.setDateOfBirth(pending);
        profile.setPlaceOfBirth(pending);
        profile.setDocumentType(pending);
        profile.setPassportNumber(pending);
        profile.setPassportIssueDate(pending);
        profile.setPassportExpiryDate(pending);
        profile.setNbs(generateDigits(9));
        profile.setNdu(generateDigits(9));
    }

    private void savePhoneContact(int profileId, String phone) {
        ProfileContact contact = new ProfileContact();
        contact.setProfileId(profileId);
        contact.setContactType("phone");
        contact.setValue(PhoneUtils.normalize(phone));
        contact.setIsMain(true);
        contact.setIsConfirmed(true);
        profileContactRepository.save(contact);
    }

    private void saveEmailContact(int profileId, String email) {
        ProfileContact contact = new ProfileContact();
        contact.setProfileId(profileId);
        contact.setContactType("email");
        contact.setValue(email);
        contact.setIsMain(true);
        contact.setIsConfirmed(false);
        profileContactRepository.save(contact);
    }

    private void saveRegistrationAddress(int profileId) {
        ProfileAddress address = new ProfileAddress();
        address.setProfileId(profileId);
        address.setCountry("GE");
        address.setCity("Tbilisi");
        address.setAddress("PENDING");
        address.setIsMain(true);
        address.setIsConfirmed(true);
        address.setAddressType("registration");
        profileAddressRepository.save(address);
    }

    private String generateAccountNumber() {
        return "RC-BA" + generateDigits(7);
    }

    private String generateDigits(int length) {
        long min = (long) Math.pow(10, length - 1);
        long max = (long) Math.pow(10, length) - 1;
        return String.valueOf(ThreadLocalRandom.current().nextLong(min, max + 1));
    }
}
