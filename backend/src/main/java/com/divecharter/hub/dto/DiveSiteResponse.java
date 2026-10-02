package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.Difficulty;

public record DiveSiteResponse(
        Long id,
        String name,
        String location,
        Integer maxDepthMeters,
        Difficulty difficulty,
        String description
) {
}