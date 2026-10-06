package com.divecharter.hub.security;

import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.support.TestData;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService userDetailsService;

    @InjectMocks private JwtAuthFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/bookings/me");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    @Test
    void noAuthorizationHeader_leavesRequestAnonymous() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/trips"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull(); // request continued down the chain
    }

    @Test
    void validToken_logsUserIn() throws Exception {
        User user = TestData.diver(1L, CertificationLevel.OPEN_WATER, true);
        when(jwtService.extractEmail("good-token")).thenReturn(user.getEmail());
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(jwtService.isTokenValid("good-token", user)).thenReturn(true);

        filter.doFilter(requestWithToken("good-token"), new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isSameAs(user);
    }

    @Test
    void invalidToken_isIgnored_andRequestContinues() throws Exception {
        when(jwtService.extractEmail("bad-token")).thenThrow(new MalformedJwtException("bad"));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWithToken("bad-token"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }
}