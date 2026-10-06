package com.divecharter.hub.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSecurityErrorHandlerTest {

    private final JsonSecurityErrorHandler handler = new JsonSecurityErrorHandler();

    @Test
    void notLoggedIn_returns401Json() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(new MockHttpServletRequest("POST", "/api/bookings"), response,
                new BadCredentialsException("no token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString())
                .contains("\"status\":401")
                .contains("Authentication is required")
                .contains("/api/bookings");
    }

    @Test
    void wrongRole_returns403Json() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest("GET", "/api/admin/users"), response,
                new AccessDeniedException("not admin"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"status\":403");
    }
}