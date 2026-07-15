package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.dto.AuthRequest;
import com.rioni.lk.api.dto.AuthResponse;
import com.rioni.lk.api.dto.SmsCodeResponse;
import com.rioni.lk.api.exception.AuthException;
import com.rioni.lk.api.exception.SmsCodeInvalidException;
import com.rioni.lk.api.exception.SmsCodeNotFoundException;
import com.rioni.lk.api.model.ProfileContact;
import com.rioni.lk.api.model.ProfilePassword;
import com.rioni.lk.api.model.SmsCode;
import com.rioni.lk.api.repository.ProfileContactRepository;
import com.rioni.lk.api.repository.ProfilePasswordRepository;
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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEV_SMS_CODE = "111111";
    private static final int SMS_CODE_VALIDITY_MINUTES = 5;

    private final ProfilePasswordRepository profilePasswordRepository;
    private final ProfileContactRepository profileContactRepository;
    private final SmsCodeRepository smsCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public AuthServiceImpl(
            ProfilePasswordRepository profilePasswordRepository,
            ProfileContactRepository profileContactRepository,
            SmsCodeRepository smsCodeRepository,
            PasswordEncoder passwordEncoder,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.profilePasswordRepository = profilePasswordRepository;
        this.profileContactRepository = profileContactRepository;
        this.smsCodeRepository = smsCodeRepository;
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
        String code = DEV_SMS_CODE;
        
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(PhoneUtils.normalize(phone));
        smsCode.setCode(code);
        smsCode.setCreatedAt(LocalDateTime.now());
        
        SmsCode savedSmsCode = smsCodeRepository.save(smsCode);
        
        return new AuthResponse(
                null,
                null,
                null,
                0,
                0,
                savedSmsCode.getId()
        );
    }

    @Override
    public SmsCodeResponse sendSmsCode(String phone) {
        String code = DEV_SMS_CODE;
        
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(PhoneUtils.normalize(phone));
        smsCode.setCode(code);
        smsCode.setCreatedAt(LocalDateTime.now());
        
        SmsCode savedSmsCode = smsCodeRepository.save(smsCode);
        
        return new SmsCodeResponse(savedSmsCode.getId());
    }

    @Override
    @Transactional
    public AuthResponse checkSmsCode(long smsCodeId, String code) {
        Optional<SmsCode> smsCodeOpt = smsCodeRepository.findById(smsCodeId);
        
        if (smsCodeOpt.isEmpty()) {
            throw new SmsCodeNotFoundException("Sms code none exist");
        }
        
        SmsCode smsCode = smsCodeOpt.get();
        
        if (smsCode.isUsed()) {
            throw new SmsCodeInvalidException("Wrong code");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = smsCode.getCreatedAt();
        LocalDateTime expiresAt = createdAt.plusMinutes(SMS_CODE_VALIDITY_MINUTES);
        
        if (now.isAfter(expiresAt)) {
            throw new SmsCodeInvalidException("Wrong code");
        }
        
        if (!smsCode.getCode().equals(code)) {
            throw new SmsCodeInvalidException("Wrong code");
        }
        
        String phone = smsCode.getPhone();
        
        Optional<ProfileContact> phoneContact = profileContactRepository
                .findTopByValueAndContactTypeOrderByIsMainDesc(phone, "phone");
        
        if (phoneContact.isEmpty()) {
            throw new RuntimeException("Profile not found for this phone");
        }
        
        int profileId = phoneContact.get().getProfileId();
        
        smsCode.setUsed(true);
        smsCodeRepository.save(smsCode);
        
        String accessToken = generateAccessToken(profileId);
        String refreshToken = generateRefreshToken(profileId);
        
        return new AuthResponse(
                accessToken,
                "Bearer",
                refreshToken,
                accessTokenExpiration,
                refreshTokenExpiration,
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
}
