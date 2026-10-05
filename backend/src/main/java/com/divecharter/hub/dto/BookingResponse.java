package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long tripId,
        String siteName,
        String boatName,
        LocalDateTime departureTime,
        LocalDateTime returnTime,
        BookingStatus status,
        BigDecimal totalPrice,
        LocalDateTime bookedAt
) {
}