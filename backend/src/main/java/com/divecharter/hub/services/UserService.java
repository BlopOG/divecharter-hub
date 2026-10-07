package com.divecharter.hub.services;

import com.divecharter.hub.dto.CommonDtos.PageResponse;
import com.divecharter.hub.dto.UserDtos.UserResponse;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.Role;
import com.divecharter.hub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /** All divers, or only those still waiting for certification verification. */
    public PageResponse<UserResponse> listDivers(boolean pendingOnly, Pageable pageable) {
        Page<User> page = pendingOnly
                ? userRepository.findByRoleAndCertVerifiedFalse(Role.USER, pageable)
                : userRepository.findByRole(Role.USER, pageable);
        return PageResponse.from(page.map(AuthService::toResponse));
    }

    /** Admin marks a diver's certification card as checked (or revokes it). */
    @Transactional
    public UserResponse setCertificationVerified(Long userId, boolean verified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setCertVerified(verified);
        return AuthService.toResponse(user);
    }
}