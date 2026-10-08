package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.Difficulty;
import com.divecharter.hub.models.enums.TripStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** The JSON that goes in and out of the API for dive sites and trips. */
public final class DiveDtos {

    private DiveDtos() {
    }

    // ---------- IN (requests) ----------

    public record DiveSiteRequest(
            @NotBlank(message = "Name is required")
            @Size(max = 100, message = "Name must be at most 100 characters")
            String name,

            @NotBlank(message = "Location is required")
            @Size(max = 150, message = "Location must be at most 150 characters")
            String location,

            @NotNull(message = "Max depth is required")
            @Min(value = 1, message = "Max depth must be at least 1 meter")
            @Max(value = 40, message = "Max depth cannot exceed the 40 m recreational limit")
            Integer maxDepthMeters,

            @NotNull(message = "Difficulty is required")
            Difficulty difficulty,

            @Size(max = 2000, message = "Description must be at most 2000 characters")
            String description,

            @Size(max = 255, message = "Image URL must be at most 255 characters")
            @Pattern(regexp = "^(/|https://).*$", message = "Image URL must start with / or https://")
            String imageUrl
    ) {
    }

    // ---------- OUT (responses) ----------

    public record DiveSiteResponse(
            Long id,
            String name,
            String location,
            Integer maxDepthMeters,
            Difficulty difficulty,
            String description,
            String imageUrl
    ) {
    }

    public record DiveTripResponse(
            Long id,
            Long siteId,
            String siteName,
            String siteLocation,
            Integer maxDepthMeters,
            Difficulty difficulty,
            String boatName,
            LocalDateTime departureTime,
            LocalDateTime returnTime,
            Integer capacity,
            int seatsBooked,
            int seatsAvailable,
            BigDecimal price,
            TripStatus status,
            String siteImageUrl
    ) {
    }
}