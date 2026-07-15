package com.rioni.lk.api.controller;

import com.rioni.lk.api.AbstractIntegrationTest;
import com.rioni.lk.api.dto.AuthRequest;
import com.rioni.lk.api.dto.AuthResponse;
import com.rioni.lk.api.dto.SmsCodeRequest;
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
    }

    @Test
    void login_withValidCredentials_shouldReturn200AndSmsCodeId() {
        // Arrange
        AuthRequest request = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> httpRequest = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                httpRequest,
                AuthResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // On first stage (password only), we get an sms_code_id back, no tokens
        assertThat(response.getBody().getAccessToken()).isNull();
        assertThat(response.getBody().getRefreshToken()).isNull();
        assertThat(response.getBody().getSmsCodeId()).isPositive();
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

    // ─────────────────────────────────────────────────────────────────────────────
    //  /auth/check_sms
    // ─────────────────────────────────────────────────────────────────────────────
    private static final String TEST_SMS_CODE = "111111";

    @Test
    void checkSms_withValidCode_shouldReturn200AndTokens() {
        // Arrange — first login to create an SMS code
        AuthRequest loginRequest = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequest> loginHttpEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<AuthResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                AuthResponse.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().getSmsCodeId()).isPositive();
        long smsCodeId = loginResponse.getBody().getSmsCodeId();

        // Act — check SMS with the dev code
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, TEST_SMS_CODE);
        HttpEntity<SmsCodeRequest> smsHttpEntity = new HttpEntity<>(smsRequest, headers);

        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check_sms",
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
        // sms_code_id should be null because the SMS was used and tokens are returned
        assertThat(response.getBody().getSmsCodeId()).isNull();
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
                baseUrl() + "/api/auth/check_sms",
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
                baseUrl() + "/api/auth/check_sms",
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
                "INSERT INTO sms_codes (phone, code, created_at, is_used) VALUES (?, ?, NOW() - INTERVAL '1 hour', ?)",
                TEST_PHONE, TEST_SMS_CODE, false
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
                baseUrl() + "/api/auth/check_sms",
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

        ResponseEntity<AuthResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                AuthResponse.class
        );
        assertThat(loginResponse.getBody()).isNotNull();
        long smsCodeId = loginResponse.getBody().getSmsCodeId();

        // Act — use a wrong code
        SmsCodeRequest request = new SmsCodeRequest(smsCodeId, "000000");
        HttpEntity<SmsCodeRequest> httpRequest = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/check_sms",
                HttpMethod.POST,
                httpRequest,
                String.class
        );

        // Assert — SmsCodeInvalidException → 400 with "Wrong code"
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Wrong code");
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
        // sms_code_id should be null (not an SMS stage)
        assertThat(response.getBody().getSmsCodeId()).isNull();
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
        assertThat(response.getBody().getSmsCodeId()).isNull();
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

        ResponseEntity<AuthResponse> loginResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                loginHttpEntity,
                AuthResponse.class
        );
        long smsCodeId = loginResponse.getBody().getSmsCodeId();

        // Step 2: consume the code
        SmsCodeRequest smsRequest = new SmsCodeRequest(smsCodeId, TEST_SMS_CODE);
        HttpEntity<SmsCodeRequest> smsHttpEntity = new HttpEntity<>(smsRequest, headers);
        restTemplate.exchange(
                baseUrl() + "/api/auth/check_sms",
                HttpMethod.POST,
                smsHttpEntity,
                AuthResponse.class
        );

        return smsCodeId;
    }
}
