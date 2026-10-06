package com.divecharter.hub.security;

import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.support.TestData;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // Test-only secret (32 bytes, base64). Never your real JWT_SECRET.
    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHktMzJieXRlcyE=";

    private final JwtService jwtService = new JwtService(SECRET, 60_000);
    private final User user = TestData.diver(1L, CertificationLevel.OPEN_WATER, true);

    @Test
    void generatedToken_isValidForSameUser() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo(user.getEmail());
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
        assertThat(jwtService.getExpirationMs()).isEqualTo(60_000);
    }

    @Test
    void token_isNotValidForADifferentUser() {
        String token = jwtService.generateToken(user);
        User other = TestData.diver(2L, CertificationLevel.OPEN_WATER, true);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void tamperedToken_isRejected() {
        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        assertThatThrownBy(() -> jwtService.extractEmail(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void expiredToken_isRejected() {
        JwtService shortLived = new JwtService(SECRET, -1_000);
        String token = shortLived.generateToken(user);

        assertThatThrownBy(() -> shortLived.extractEmail(token)).isInstanceOf(JwtException.class);
    }
}