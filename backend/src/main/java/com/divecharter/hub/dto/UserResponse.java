package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.models.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        CertificationLevel certLevel,
        String certAgency,
        String certNumber,
        boolean certVerified,
        int maxDepthMeters,
        LocalDateTime createdAt
) {
}