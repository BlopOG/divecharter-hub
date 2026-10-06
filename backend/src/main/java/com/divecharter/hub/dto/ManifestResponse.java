package com.divecharter.hub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ManifestResponse(
        Long tripId,
        String siteName,
        String boatName,
        LocalDateTime departureTime,
        LocalDateTime returnTime,
        int capacity,
        int seatsBooked,
        List<ManifestEntry> passengers
) {
}