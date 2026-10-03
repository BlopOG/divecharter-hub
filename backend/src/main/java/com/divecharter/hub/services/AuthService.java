package com.divecharter.hub.services;

import com.divecharter.hub.dto.RegisterRequest;
import com.divecharter.hub.dto.UserResponse;
import com.divecharter.hub.exceptions.DuplicateResourceException;
import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.Role;
import com.divecharter.hub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(Role.USER);
        user.setCertLevel(request.certLevel());
        user.setCertAgency(trimOrNull(request.certAgency()));
        user.setCertNumber(trimOrNull(request.certNumber()));
        user.setCertVerified(false);

        return toResponse(userRepository.save(user));
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

    private static String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}