package com.divecharter.hub.dto;

import jakarta.validation.constraints.NotNull;

public record CertificationVerificationRequest(
        @NotNull(message = "verified is required") Boolean verified
) {
}