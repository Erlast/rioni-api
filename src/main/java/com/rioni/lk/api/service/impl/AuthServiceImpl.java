package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.AuthRequest;
import com.rioni.lk.api.dto.AuthResponse;
import com.rioni.lk.api.dto.ChangePasswordRequest;
import com.rioni.lk.api.dto.CheckContactRequest;
import com.rioni.lk.api.dto.CheckContactResponse;
import com.rioni.lk.api.dto.RecoverSmsRequest;
import com.rioni.lk.api.dto.SmsCodeRequest;
import com.rioni.lk.api.dto.SmsCodeResponse;
import com.rioni.lk.api.exception.AuthException;
import com.rioni.lk.api.exception.DataNotFoundException;
import com.rioni.lk.api.exception.SmsCodeAttemptsExceededException;
import com.rioni.lk.api.exception.SmsCodeInvalidException;
import com.rioni.lk.api.exception.SmsCodeNotFoundException;
import com.rioni.lk.api.model.ProfileContact;
import com.rioni.lk.api.model.ProfilePassword;
import com.rioni.lk.api.model.SmsCode;
import com.rioni.lk.api.repository.ProfileContactRepository;
import com.rioni.lk.api.repository.ProfilePasswordRepository;
import com.rioni.lk.api.repository.ProfileRepository;
import com.rioni.lk.api.repository.SmsCodeRepository;
import com.rioni.lk.api.service.AuthService;
import com.rioni.lk.api.util.PhoneUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEV_SMS_CODE = "111111";
    private static final int SMS_CODE_VALIDITY_MINUTES = 5;
    private static final int SMS_CODE_BLOCK_DURATION_MINUTES = 30;

    private final ProfilePasswordRepository profilePasswordRepository;
    private final ProfileContactRepository profileContactRepository;
    private final SmsCodeRepository smsCodeRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public AuthServiceImpl(
            ProfilePasswordRepository profilePasswordRepository,
            ProfileContactRepository profileContactRepository,
            SmsCodeRepository smsCodeRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.profilePasswordRepository = profilePasswordRepository;
        this.profileContactRepository = profileContactRepository;
        this.smsCodeRepository = smsCodeRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
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

        // Check if the latest SMS code for this phone has exceeded the attempt limit
        // and was created within the last 30 minutes
        smsCodeRepository.findTopByPhoneOrderByCreatedAtDesc(normalizedPhone)
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
        
        return new AuthResponse(
                null,
                null,
                null,
                0,
                0,
                savedSmsCode.getId(),
                maskedPhone,
                "authorization",
                null
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

        String phone = smsCode.getPhone();

        Optional<ProfileContact> phoneContact = profileContactRepository
                .findTopByValueAndContactTypeOrderByIsMainDesc(phone, "phone");

        if (phoneContact.isEmpty()) {
            throw new RuntimeException("Profile not found for this phone");
        }

        int profileId = phoneContact.get().getProfileId();

        // Recovery purpose: the code is confirmed and marked as used, but no
        // tokens are issued. The profile_id is returned so the change password
        // API knows which profile the password will be reset for.
        if ("recovery".equals(purpose)) {
            return new AuthResponse(
                    null,
                    null,
                    null,
                    0,
                    0,
                    smsCode.getId(),
                    null,
                    purpose,
                    profileId
            );
        }
        
        String accessToken = generateAccessToken(profileId);
        String refreshToken = generateRefreshToken(profileId);
        
        return new AuthResponse(
                accessToken,
                "Bearer",
                refreshToken,
                accessTokenExpiration,
                refreshTokenExpiration,
                null,
                null,
                purpose,
                null
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
                refreshTokenExpiration,
                null,
                null,
                null,
                null
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
    public SmsCodeResponse recoverSms(RecoverSmsRequest request) {
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

        return new SmsCodeResponse(savedSmsCode.getId());
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
}
