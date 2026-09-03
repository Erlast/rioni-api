package com.rioni.lk.api.controller;

import com.rioni.lk.api.AbstractIntegrationTest;
import com.rioni.lk.api.dto.AuthRequest;
import com.rioni.lk.api.dto.AuthResponse;
import com.rioni.lk.api.dto.ChangePasswordRequest;
import com.rioni.lk.api.dto.CheckContactRequest;
import com.rioni.lk.api.dto.CheckContactResponse;
import com.rioni.lk.api.dto.RecoverSmsRequest;
import com.rioni.lk.api.dto.RegistrationSmsRequest;
import com.rioni.lk.api.dto.SmsCodeRequest;
import com.rioni.lk.api.dto.SmsSendResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest extends AbstractIntegrationTest {

    private static final int TEST_PROFILE_ID = 100;
    private static final String TEST_LOGIN = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_PHONE = "+375291234567";
    private static final String TEST_EMAIL = "testuser@example.com";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data from previous runs
        jdbcTemplate.update("DELETE FROM profile_contacts WHERE profile_id = ?", TEST_PROFILE_ID);
        jdbcTemplate.update("DELETE FROM profile_passwords WHERE profile_id = ?", TEST_PROFILE_ID);
        jdbcTemplate.update("DELETE FROM sms_codes WHERE phone = ?", TEST_PHONE);
        jdbcTemplate.update("DELETE FROM profile WHERE id = ?", TEST_PROFILE_ID);

        // Create fresh test data with a properly BCrypt-encoded password
        String encodedPassword = passwordEncoder.encode(TEST_PASSWORD);
        jdbcTemplate.update("INSERT INTO profile (id, login) VALUES (?, ?)",
                TEST_PROFILE_ID, TEST_LOGIN);
        jdbcTemplate.update("INSERT INTO profile_passwords (profile_id, password_hash) VALUES (?, ?)",
                TEST_PROFILE_ID, encodedPassword);
        jdbcTemplate.update(
                "INSERT INTO profile_contacts (profile_id, contact_type, value, is_main, is_confirmed) VALUES (?, ?, ?, ?, ?)",
                TEST_PROFILE_ID, "phone", TEST_PHONE, true, true);
        jdbcTemplate.update(
                "INSERT INTO profile_contacts (profile_id, contact_type, value, is_main, is_confirmed) VALUES (?, ?, ?, ?, ?)",
                TEST_PROFILE_ID, "email", TEST_EMAIL, true, true);
    }

    @Test
    void login_withValidCredentials_shouldReturn200AndSmsCodeId() {
        // Arrange
        AuthRequest request = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<SmsSendResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                httpRequest,
                SmsSendResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // On first stage (password only), we get an sms_code_id back, no tokens
        assertThat(response.getBody().getSmsCodeId()).isPositive();
        assertThat(response.getBody().getPhoneMasked()).isEqualTo("+37********67");
        assertThat(response.getBody().getPurpose()).isEqualTo("authorization");
        assertThat(response.getBody().getProfileId()).isEqualTo(TEST_PROFILE_ID);
    }

    @Test
    void login_withInvalidPassword_shouldReturn401() {
        // Arrange
        AuthRequest request = new AuthRequest(TEST_LOGIN, "wrongpassword");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Wrong login or password");
    }

    @Test
    void login_withNonExistentUser_shouldReturn401() {
        // Arrange
        AuthRequest request = new AuthRequest("nonexistent", TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Wrong login or password");
    }

    @Test
    void login_withEmptyBody_shouldReturn401() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpRequest = new HttpEntity<>("{}", headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Wrong login or password");
    }

    @Test
    void login_whenSmsAttemptsExceeded_shouldReturn422() {
        // Arrange — create an SMS code via login
        AuthRequest loginRequest = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> loginHttpEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<SmsSendResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                SmsSendResponse.class
        );
        assertThat(loginResponse.getBody()).isNotNull();
        long smsCodeId = loginResponse.getBody().getSmsCodeId();

        // Exceed the attempt limit by sending wrong code 4 times
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, "000000");
        HttpEntity<SmsCodeRequest> httpEntity = new HttpEntity<>(smsRequest, headers);
        for (int i = 0; i < 4; i++) {
            restTemplate.exchange(
                    baseUrl() + "/api/auth/check-sms",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
        }

        // Act — try to login again
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                String.class
        );

        // Assert — SmsCodeAttemptsExceededException → 422
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("Attempts limit exceeded");
        assertThat(response.getBody()).contains("timeLeft");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  /auth/check-sms
    // ─────────────────────────────────────────────────────────────────────────────
    private static final String TEST_SMS_CODE = "111111";

    @Test
    void checkSms_withValidCode_shouldReturn200AndTokens() {
        // Arrange — first login to create an SMS code
        AuthRequest loginRequest = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> loginHttpEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<SmsSendResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                SmsSendResponse.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().getSmsCodeId()).isPositive();
        long smsCodeId = loginResponse.getBody().getSmsCodeId();

        // Act — check SMS with the dev code
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, TEST_SMS_CODE);
        HttpEntity<SmsCodeRequest> smsHttpEntity = new HttpEntity<>(smsRequest, headers);

        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                smsHttpEntity,
                AuthResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
        assertThat(response.getBody().getExpiresIn()).isPositive();
        assertThat(response.getBody().getRefreshTokenExpiresIn()).isPositive();

        // Verify that the SMS code in the database has purpose = 'authorization'
        String purpose = jdbcTemplate.queryForObject(
                "SELECT purpose FROM sms_codes WHERE id = ?",
                String.class, smsCodeId);
        assertThat(purpose).isEqualTo("authorization");
    }

    @Test
    void checkSms_withNonExistentId_shouldReturn404() {
        // Arrange
        SmsCodeRequest request = new SmsCodeRequest(99999L, TEST_SMS_CODE);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SmsCodeRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert — SmsCodeNotFoundException → 404
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Sms code none exist");
    }

    @Test
    void checkSms_withAlreadyUsedCode_shouldReturn400() {
        // Arrange — first create a valid SMS code, then consume it
        long smsCodeId = createAndConsumeSmsCode();

        // Now the code is marked as used; try to check it again
        SmsCodeRequest request = new SmsCodeRequest(smsCodeId, TEST_SMS_CODE);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SmsCodeRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert — SmsCodeInvalidException → 400 with "Wrong code"
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Wrong code");
    }

    @Test
    void checkSms_withExpiredCode_shouldReturn400() {
        // Arrange — insert an SMS code with a very old creation timestamp (expired)
        jdbcTemplate.update(
                "INSERT INTO sms_codes (phone, code, created_at, is_used, attempted_count, purpose) VALUES (?, ?, NOW() - INTERVAL '1 hour', ?, 0, ?)",
                TEST_PHONE, TEST_SMS_CODE, false, "authorization"
        );
        Long smsCodeId = jdbcTemplate.queryForObject(
                "SELECT id FROM sms_codes WHERE phone = ? AND code = ? AND is_used = false ORDER BY created_at DESC LIMIT 1",
                Long.class, TEST_PHONE, TEST_SMS_CODE
        );

        SmsCodeRequest request = new SmsCodeRequest(smsCodeId, TEST_SMS_CODE);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SmsCodeRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert — SmsCodeInvalidException → 400 with "Wrong code"
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Wrong code");
    }

    @Test
    void checkSms_withWrongCode_shouldReturn400() {
        // Arrange — create an SMS code (login returns one)
        AuthRequest loginRequest = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> loginHttpEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<SmsSendResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                SmsSendResponse.class
        );
        assertThat(loginResponse.getBody()).isNotNull();
        long smsCodeId = loginResponse.getBody().getSmsCodeId();

        // Act — use a wrong code
        SmsCodeRequest request = new SmsCodeRequest(smsCodeId, "000000");
        HttpEntity<SmsCodeRequest> httpRequest = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert — SmsCodeInvalidException → 400 with "Wrong code"
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Wrong code");
    }

    @Test
    void checkSms_withExceededAttempts_shouldReturn422() {
        // Arrange — create an SMS code via login
        AuthRequest loginRequest = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> loginHttpEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<SmsSendResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                SmsSendResponse.class
        );
        assertThat(loginResponse.getBody()).isNotNull();
        long smsCodeId = loginResponse.getBody().getSmsCodeId();
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, "000000");
        HttpEntity<SmsCodeRequest> httpEntity = new HttpEntity<>(smsRequest, headers);

        // Act — send wrong code 4 times; first 3 → 400, 4th → 422
        for (int i = 0; i < 3; i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl() + "/api/auth/check-sms",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Wrong code");
        }

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        // Assert — SmsCodeAttemptsExceededException → 422
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("Attempts limit exceeded");
        assertThat(response.getBody()).contains("timeLeft");
    }

    @Test
    void checkSms_withRecoveryPurpose_shouldReturn200WithEmptyBody() {
        // Arrange — create a recovery SMS code
        RecoverSmsRequest recoverRequest = new RecoverSmsRequest(TEST_EMAIL, "email");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RecoverSmsRequest> recoverHttpEntity = new HttpEntity<>(recoverRequest, headers);

        ResponseEntity<SmsSendResponse> recoverResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/recover-sms",
                HttpMethod.POST,
                recoverHttpEntity,
                SmsSendResponse.class
        );
        assertThat(recoverResponse.getBody()).isNotNull();
        long smsCodeId = recoverResponse.getBody().getSmsCodeId();

        // Act — check the recovery SMS code
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, RECOVERY_SMS_CODE);
        HttpEntity<SmsCodeRequest> smsHttpEntity = new HttpEntity<>(smsRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                smsHttpEntity,
                String.class
        );

        // Assert — 200 with an empty body, no tokens are issued for the
        // recovery flow. The SMS code ID from the recover-sms response is
        // used by the change password API.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNullOrEmpty();

        // The code should be marked as used
        Boolean isUsed = jdbcTemplate.queryForObject(
                "SELECT is_used FROM sms_codes WHERE id = ?",
                Boolean.class, smsCodeId);
        assertThat(isUsed).isTrue();
    }

    @Test
    void checkSms_withRegistrationPurpose_shouldReturn200WithEmptyBody() {
        // Arrange — create a registration SMS code for a phone that has no
        // profile yet (typical for the registration flow)
        RegistrationSmsRequest registrationRequest = new RegistrationSmsRequest("+375296666666");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegistrationSmsRequest> registrationHttpEntity = new HttpEntity<>(registrationRequest, headers);

        ResponseEntity<SmsSendResponse> registrationResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/registration-sms-code",
                HttpMethod.POST,
                registrationHttpEntity,
                SmsSendResponse.class
        );
        assertThat(registrationResponse.getBody()).isNotNull();
        long smsCodeId = registrationResponse.getBody().getSmsCodeId();

        // Act — check the registration SMS code
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, REGISTRATION_SMS_CODE);
        HttpEntity<SmsCodeRequest> smsHttpEntity = new HttpEntity<>(smsRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                smsHttpEntity,
                String.class
        );

        // Assert — 200 with an empty body, no tokens are issued for the
        // registration flow and no profile lookup is performed.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNullOrEmpty();

        // The code should be marked as used
        Boolean isUsed = jdbcTemplate.queryForObject(
                "SELECT is_used FROM sms_codes WHERE id = ?",
                Boolean.class, smsCodeId);
        assertThat(isUsed).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  /auth/refresh
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void refresh_withValidAccessToken_shouldReturn200AndNewTokens() {
        // Arrange — create a valid access token for the test user
        String accessToken = createAccessToken(TEST_PROFILE_ID);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/refresh",
                HttpMethod.GET,
                request,
                AuthResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
        assertThat(response.getBody().getExpiresIn()).isPositive();
        assertThat(response.getBody().getRefreshTokenExpiresIn()).isPositive();
    }

    @Test
    void refresh_withExpiredAccessToken_shouldReturn200AndNewTokens() {
        // Arrange — create an access token that expired 1 minute ago
        Date expiredDate = new Date(System.currentTimeMillis() - 60_000);
        String expiredAccessToken = Jwts.builder()
                .subject(String.valueOf(TEST_PROFILE_ID))
                .claim("tokenType", "access")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(
                        "testSecretKeyThatIsAtLeast32CharactersLongForJwt".getBytes(StandardCharsets.UTF_8)))
                .compact();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(expiredAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/refresh",
                HttpMethod.GET,
                request,
                AuthResponse.class
        );

        // Assert — JwtAuthenticationFilter allows expired access tokens through for refresh
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
    }

    @Test
    void refresh_withoutToken_shouldReturn403() {
        // Arrange — no Authorization header
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/refresh",
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert — Spring Security returns 403 for unauthenticated requests
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void refresh_withRefreshToken_shouldReturn403() {
        // Arrange — use a refresh token instead of an access token
        String refreshToken = createRefreshToken(TEST_PROFILE_ID);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(refreshToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/refresh",
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert — JwtAuthenticationFilter only accepts "access" tokenType,
        // so no authentication is set and Spring Security returns 403
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void refresh_withInvalidToken_shouldReturn403() {
        // Arrange — malformed token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.jwt.token");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/refresh",
                HttpMethod.GET,
                request,
                String.class
        );

        // Assert — JwtAuthenticationFilter catches the exception and clears context,
        // so no authentication is set and Spring Security returns 403
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  /auth/check-contact
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void checkContact_withExistingPhone_shouldReturnExistsTrue() {
        // Arrange
        CheckContactRequest request = new CheckContactRequest(TEST_PHONE, "phone");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CheckContactRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<CheckContactResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-contact",
                HttpMethod.POST,
                httpRequest,
                CheckContactResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isExists()).isTrue();
    }

    @Test
    void checkContact_withNonExistingPhone_shouldReturnExistsFalse() {
        // Arrange
        CheckContactRequest request = new CheckContactRequest("+375291111111", "phone");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CheckContactRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<CheckContactResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-contact",
                HttpMethod.POST,
                httpRequest,
                CheckContactResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isExists()).isFalse();
    }

    @Test
    void checkContact_withExistingEmail_shouldReturnExistsTrue() {
        // Arrange
        CheckContactRequest request = new CheckContactRequest(TEST_EMAIL, "email");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CheckContactRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<CheckContactResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-contact",
                HttpMethod.POST,
                httpRequest,
                CheckContactResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isExists()).isTrue();
    }

    @Test
    void checkContact_withNonExistingEmail_shouldReturnExistsFalse() {
        // Arrange
        CheckContactRequest request = new CheckContactRequest("nonexistent@example.com", "email");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CheckContactRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<CheckContactResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-contact",
                HttpMethod.POST,
                httpRequest,
                CheckContactResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isExists()).isFalse();
    }

    @Test
    void checkContact_withInvalidType_shouldReturnExistsFalse() {
        // Arrange
        CheckContactRequest request = new CheckContactRequest("somevalue", "invalid_type");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CheckContactRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<CheckContactResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-contact",
                HttpMethod.POST,
                httpRequest,
                CheckContactResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isExists()).isFalse();
    }

    @Test
    void checkContact_withExistingLogin_shouldReturnExistsTrue() {
        // Arrange
        CheckContactRequest request = new CheckContactRequest(TEST_LOGIN, "login");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CheckContactRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<CheckContactResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-contact",
                HttpMethod.POST,
                httpRequest,
                CheckContactResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isExists()).isTrue();
    }

    @Test
    void checkContact_withNonExistingLogin_shouldReturnExistsFalse() {
        // Arrange
        CheckContactRequest request = new CheckContactRequest("nonexistentuser", "login");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CheckContactRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<CheckContactResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check-contact",
                HttpMethod.POST,
                httpRequest,
                CheckContactResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isExists()).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  /auth/recover-sms
    // ─────────────────────────────────────────────────────────────────────────────

    private static final String RECOVERY_SMS_CODE = "222222";

    @Test
    void recoverSms_withValidEmail_shouldReturn200AndSmsCodeId() {
        // Arrange
        RecoverSmsRequest request = new RecoverSmsRequest(TEST_EMAIL, "email");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RecoverSmsRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<SmsSendResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/recover-sms",
                HttpMethod.POST,
                httpRequest,
                SmsSendResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSmsCodeId()).isPositive();
        assertThat(response.getBody().getPurpose()).isEqualTo("recovery");
        assertThat(response.getBody().getProfileId()).isZero();

        // Verify the SMS code record in the database
        String purpose = jdbcTemplate.queryForObject(
                "SELECT purpose FROM sms_codes WHERE id = ?",
                String.class, response.getBody().getSmsCodeId());
        assertThat(purpose).isEqualTo("recovery");

        String code = jdbcTemplate.queryForObject(
                "SELECT code FROM sms_codes WHERE id = ?",
                String.class, response.getBody().getSmsCodeId());
        assertThat(code).isEqualTo(RECOVERY_SMS_CODE);

        Boolean isUsed = jdbcTemplate.queryForObject(
                "SELECT is_used FROM sms_codes WHERE id = ?",
                Boolean.class, response.getBody().getSmsCodeId());
        assertThat(isUsed).isFalse();
    }

    @Test
    void recoverSms_withValidPhone_shouldReturn200AndSmsCodeId() {
        // Arrange
        RecoverSmsRequest request = new RecoverSmsRequest(TEST_PHONE, "phone");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RecoverSmsRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<SmsSendResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/recover-sms",
                HttpMethod.POST,
                httpRequest,
                SmsSendResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSmsCodeId()).isPositive();
    }

    @Test
    void recoverSms_withNonExistentEmail_shouldReturn422() {
        // Arrange
        RecoverSmsRequest request = new RecoverSmsRequest("nonexistent@example.com", "email");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RecoverSmsRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/recover-sms",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("Data not found");
    }

    @Test
    void recoverSms_withNonExistentPhone_shouldReturn422() {
        // Arrange
        RecoverSmsRequest request = new RecoverSmsRequest("+375291111111", "phone");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RecoverSmsRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/recover-sms",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("Data not found");
    }

    @Test
    void recoverSms_withInvalidType_shouldReturn422() {
        // Arrange
        RecoverSmsRequest request = new RecoverSmsRequest("somevalue", "invalid_type");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RecoverSmsRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/recover-sms",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("Data not found");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  /auth/registration-sms-code
    // ─────────────────────────────────────────────────────────────────────────────

    private static final String REGISTRATION_SMS_CODE = "333333";

    @Test
    void registrationSmsCode_withValidPhone_shouldReturn200AndSmsCodeId() {
        // Arrange — phone with formatting that must be normalized
        RegistrationSmsRequest request = new RegistrationSmsRequest("+375 (29) 123-45-67");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegistrationSmsRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<SmsSendResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/registration-sms-code",
                HttpMethod.POST,
                httpRequest,
                SmsSendResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSmsCodeId()).isPositive();
        assertThat(response.getBody().getPurpose()).isEqualTo("registration");
        assertThat(response.getBody().getProfileId()).isZero();

        // Verify the SMS code record in the database
        long smsCodeId = response.getBody().getSmsCodeId();

        String phone = jdbcTemplate.queryForObject(
                "SELECT phone FROM sms_codes WHERE id = ?",
                String.class, smsCodeId);
        assertThat(phone).isEqualTo(TEST_PHONE);

        String purpose = jdbcTemplate.queryForObject(
                "SELECT purpose FROM sms_codes WHERE id = ?",
                String.class, smsCodeId);
        assertThat(purpose).isEqualTo("registration");

        String code = jdbcTemplate.queryForObject(
                "SELECT code FROM sms_codes WHERE id = ?",
                String.class, smsCodeId);
        assertThat(code).isEqualTo(REGISTRATION_SMS_CODE);

        Boolean isUsed = jdbcTemplate.queryForObject(
                "SELECT is_used FROM sms_codes WHERE id = ?",
                Boolean.class, smsCodeId);
        assertThat(isUsed).isFalse();
    }

    @Test
    void registrationSmsCode_withBlankPhone_shouldReturn400() {
        // Arrange
        RegistrationSmsRequest request = new RegistrationSmsRequest("   ");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegistrationSmsRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/registration-sms-code",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert — SmsCodeSendException → 400 with "Failed to send code"
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Failed to send code");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  /auth/change_password
    // ─────────────────────────────────────────────────────────────────────────────

    private static final String NEW_PASSWORD = "newpassword456";

    @Test
    void changePassword_withConfirmedRecoveryCode_shouldReturn200AndUpdatePassword() {
        // Arrange — create and confirm a recovery SMS code
        long smsCodeId = createAndConsumeRecoverySmsCode();

        ChangePasswordRequest request = new ChangePasswordRequest(null, NEW_PASSWORD, smsCodeId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChangePasswordRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/api/auth/change_password",
                HttpMethod.POST,
                httpRequest,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // The new password should work for login
        AuthRequest loginRequest = new AuthRequest(TEST_LOGIN, NEW_PASSWORD);
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> loginHttpRequest = new HttpEntity<>(loginRequest, loginHeaders);

        ResponseEntity<SmsSendResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpRequest,
                SmsSendResponse.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // The old password should no longer work
        AuthRequest oldLoginRequest = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpEntity<AuthRequest> oldLoginHttpRequest = new HttpEntity<>(oldLoginRequest, loginHeaders);

        ResponseEntity<String> oldLoginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                oldLoginHttpRequest,
                String.class
        );
        assertThat(oldLoginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void changePassword_withWrongOldPassword_shouldReturn401() {
        // Arrange — create and confirm a recovery SMS code
        long smsCodeId = createAndConsumeRecoverySmsCode();

        ChangePasswordRequest request = new ChangePasswordRequest("wrong-old-password", NEW_PASSWORD, smsCodeId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChangePasswordRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/change_password",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Wrong old password");
    }

    @Test
    void changePassword_withNonRecoveryCode_shouldReturn400() {
        // Arrange — create an authorization SMS code (not recovery)
        long smsCodeId = createAndConsumeSmsCode();

        ChangePasswordRequest request = new ChangePasswordRequest(null, NEW_PASSWORD, smsCodeId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChangePasswordRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/change_password",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert — SmsCodeInvalidException → 400
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Wrong code");
    }

    @Test
    void changePassword_withNonExistentCode_shouldReturn404() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(null, NEW_PASSWORD, 999999L);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChangePasswordRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/change_password",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert — SmsCodeNotFoundException → 404
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Sms code none exist");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Logs in with the test user to create an SMS code, then uses it, so
     * the code becomes marked as {@code is_used = true}. Returns the SMS
     * code ID for further negative testing.
     */
    private long createAndConsumeSmsCode() {
        // Step 1: login to create an SMS code
        AuthRequest loginRequest = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> loginHttpEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<SmsSendResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                SmsSendResponse.class
        );
        long smsCodeId = loginResponse.getBody().getSmsCodeId();

        // Step 2: consume the code
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, TEST_SMS_CODE);
        HttpEntity<SmsCodeRequest> smsHttpEntity = new HttpEntity<>(smsRequest, headers);
        restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                smsHttpEntity,
                AuthResponse.class
        );

        return smsCodeId;
    }

    /**
     * Creates a recovery SMS code via /auth/recover-sms and confirms it via
     * /auth/check-sms (marking it {@code is_used = true}). Returns the SMS
     * code ID for the change password API.
     */
    private long createAndConsumeRecoverySmsCode() {
        // Step 1: recover-sms to create a recovery code
        RecoverSmsRequest recoverRequest = new RecoverSmsRequest(TEST_EMAIL, "email");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RecoverSmsRequest> recoverHttpEntity = new HttpEntity<>(recoverRequest, headers);

        ResponseEntity<SmsSendResponse> recoverResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/recover-sms",
                HttpMethod.POST,
                recoverHttpEntity,
                SmsSendResponse.class
        );
        long smsCodeId = recoverResponse.getBody().getSmsCodeId();

        // Step 2: confirm the recovery code via check-sms
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, RECOVERY_SMS_CODE);
        HttpEntity<SmsCodeRequest> smsHttpEntity = new HttpEntity<>(smsRequest, headers);
        restTemplate.exchange(
                baseUrl() + "/api/auth/check-sms",
                HttpMethod.POST,
                smsHttpEntity,
                AuthResponse.class
        );

        return smsCodeId;
    }
}
