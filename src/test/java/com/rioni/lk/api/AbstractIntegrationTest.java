package com.rioni.lk.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Base class for integration tests.
 * <p>
 * Provides:
 * <ul>
 *   <li>JWT token generation helper methods ({@link #createAccessToken(Integer)},
 *       {@link #createRefreshToken(Integer)})</li>
 *   <li>Dynamically allocated local server port via {@link #port}</li>
 *   <li>Base URL builder via {@link #baseUrl()}</li>
 * </ul>
 * <p>
 * <b>Prerequisites:</b>
 * <ol>
 *   <li>Start PostgreSQL: {@code docker compose up -d}</li>
 *   <li>Create test schema: {@code docker compose exec -T db psql -U rioni_db -c 'CREATE SCHEMA IF NOT EXISTS rioni_test;'}</li>
 *   <li>Run tests: {@code ./gradlew test}</li>
 * </ol>
 * <p>
 * On CI/CD, set these environment variables:
 * <ul>
 *   <li>{@code TEST_DB_HOST}, {@code TEST_DB_PORT}, {@code TEST_DB_NAME}, {@code TEST_DB_SCHEMA}</li>
 *   <li>{@code TEST_DB_USERNAME}, {@code TEST_DB_PASSWORD}</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    private static final String JWT_SECRET = "testSecretKeyThatIsAtLeast32CharactersLongForJwt";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * Creates a valid JWT access token for the given profile ID.
     * Uses the same secret key and token format as the application's
     * {@link com.rioni.lk.api.security.JwtAuthenticationFilter}.
     *
     * @param profileId the profile ID to encode in the token subject
     * @return a signed JWT access token string
     */
    protected String createAccessToken(Integer profileId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600_000); // 1 hour

        return Jwts.builder()
                .subject(String.valueOf(profileId))
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Creates a valid JWT refresh token for the given profile ID.
     *
     * @param profileId the profile ID to encode in the token subject
     * @return a signed JWT refresh token string
     */
    protected String createRefreshToken(Integer profileId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 604_800_000); // 7 days

        return Jwts.builder()
                .subject(String.valueOf(profileId))
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Builds the base URL for REST calls using the dynamically allocated port.
     */
    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
