package com.divecharter.hub.dto;

import com.divecharter.hub.models.enums.BookingStatus;
import com.divecharter.hub.models.enums.CertificationLevel;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** The JSON that goes in and out of the API for bookings and passenger manifests. */
public final class BookingDtos {

    private BookingDtos() {
    }

    // ---------- IN (requests) ----------

    /** Only the trip id: the diver always comes from the login token. */
    public record BookingRequest(
            @NotNull(message = "Trip id is required") Long tripId
    ) {
    }

    // ---------- OUT (responses) ----------

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

    /** One passenger on a trip's manifest. */
    public record ManifestEntry(
            Long bookingId,
            String fullName,
            String email,
            CertificationLevel certLevel,
            String certAgency,
            String certNumber
    ) {
    }

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
}