package com.divecharter.hub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits POST /api/auth/** (login and register) to a set number of attempts
 * per IP address per time window, to slow down password guessing.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final int maxAttempts;
    private final long windowMs;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(@Value("${app.rate-limit.auth-max-attempts}") int maxAttempts,
                               @Value("${app.rate-limit.auth-window-seconds}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowSeconds * 1000;
    }

    // Only apply to POST requests under /api/auth/
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/api/auth/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long now = System.currentTimeMillis();

        // Start a new window if this IP has none or its window has expired; otherwise count one more attempt
        Window window = windows.compute(request.getRemoteAddr(), (ip, current) ->
                current == null || now - current.start() >= windowMs
                        ? new Window(now, 1)
                        : new Window(current.start(), current.count() + 1));

        if (window.count() > maxAttempts) {
            long retryAfterSeconds = Math.max(1, (window.start() + windowMs - now) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(String.format(
                    "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\","
                            + "\"message\":\"Too many attempts. Try again in %d seconds.\",\"path\":\"%s\"}",
                    Instant.now(), retryAfterSeconds, request.getRequestURI()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private record Window(long start, int count) {
    }
}