package com.divecharter.hub.dto;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull(message = "Trip id is required") Long tripId
) {
}