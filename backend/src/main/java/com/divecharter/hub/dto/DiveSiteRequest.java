package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
        String description
) {
}