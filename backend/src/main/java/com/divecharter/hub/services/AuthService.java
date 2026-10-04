package com.divecharter.hub.services;

import com.divecharter.hub.dto.AuthResponse;
import com.divecharter.hub.dto.LoginRequest;
import com.divecharter.hub.dto.RegisterRequest;
import com.divecharter.hub.dto.UserResponse;
import com.divecharter.hub.exceptions.DuplicateResourceException;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.Role;
import com.divecharter.hub.repositories.UserRepository;
import com.divecharter.hub.security.JwtService;
import com.divecharter.hub.utils.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(InputSanitizer.clean(request.fullName()));
        user.setRole(Role.USER);
        user.setCertLevel(request.certLevel());
        user.setCertAgency(InputSanitizer.clean(request.certAgency()));
        user.setCertNumber(InputSanitizer.clean(request.certNumber()));
        user.setCertVerified(false);

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        // Throws BadCredentialsException (→ 401) if the email or password is wrong
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return new AuthResponse(jwtService.generateToken(user), "Bearer",
                jwtService.getExpirationMs(), toResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .map(AuthService::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCertLevel(),
                user.getCertAgency(),
                user.getCertNumber(),
                user.isCertVerified(),
                user.getCertLevel().getMaxDepthMeters(),
                user.getCreatedAt());
    }
}