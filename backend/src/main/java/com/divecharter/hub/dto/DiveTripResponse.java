package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.Difficulty;
import com.divecharter.hub.models.enums.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        TripStatus status
) {
}