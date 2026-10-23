package app.services;

import app.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-32-bytes-long";
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);

        user = new User();
        user.setEmail("test@test.com");
    }

    @Test
    void generateToken_shouldReturnToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken(user);

        boolean valid = jwtService.validateToken(token);

        assertTrue(valid);
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String invalidToken = "this.is.not.a.valid.jwt";

        boolean valid = jwtService.validateToken(invalidToken);

        assertFalse(valid);
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    void validateToken_shouldReturnFalseForTokenWithoutEmail() {
        String token = Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertFalse(jwtService.validateToken(token));
    }
}
