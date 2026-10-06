package com.divecharter.hub.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    // 2 attempts per 60 seconds, so the 3rd should be blocked
    private final AuthRateLimitFilter filter = new AuthRateLimitFilter(2, 60);

    private MockHttpServletResponse send(String method, String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void blocksLoginAttemptsOverTheLimit() throws Exception {
        assertThat(send("POST", "/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(send("POST", "/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);

        MockHttpServletResponse blocked = send("POST", "/api/auth/login", "10.0.0.1");

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isNotNull();
        assertThat(blocked.getContentAsString()).contains("Too many attempts");
    }

    @Test
    void limitsAreTrackedPerIpAddress() throws Exception {
        send("POST", "/api/auth/login", "10.0.0.2");
        send("POST", "/api/auth/login", "10.0.0.2");

        assertThat(send("POST", "/api/auth/login", "10.0.0.3").getStatus()).isEqualTo(200);
    }

    @Test
    void doesNotLimitOtherEndpoints() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(send("GET", "/api/trips", "10.0.0.4").getStatus()).isEqualTo(200);
        }
    }
}