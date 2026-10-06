package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.CertificationLevel;

public record ManifestEntry(
        Long bookingId,
        String fullName,
        String email,
        CertificationLevel certLevel,
        String certAgency,
        String certNumber
) {
}