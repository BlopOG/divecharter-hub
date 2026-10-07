package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.models.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** The JSON that goes in and out of the API for user accounts. */
public final class UserDtos {

    private UserDtos() {
    }

    // ---------- IN (requests) ----------

    public record RegisterRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be a valid address")
            @Size(max = 255)
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                    message = "Password must contain at least one letter and one number")
            String password,

            @NotBlank(message = "Full name is required")
            @Size(max = 100, message = "Full name must be at most 100 characters")
            String fullName,

            @NotNull(message = "Certification level is required")
            CertificationLevel certLevel,

            @Size(max = 50, message = "Agency must be at most 50 characters")
            String certAgency,

            @Size(max = 50, message = "Certification number must be at most 50 characters")
            String certNumber
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required") @Email(message = "Email must be a valid address") String email,
            @NotBlank(message = "Password is required") String password
    ) {
    }

    public record CertificationVerificationRequest(
            @NotNull(message = "verified is required") Boolean verified
    ) {
    }

    // ---------- OUT (responses) ----------

    /** Never includes the password hash. */
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

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInMs,
            UserResponse user
    ) {
    }
}