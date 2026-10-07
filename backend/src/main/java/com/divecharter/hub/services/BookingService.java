package com.divecharter.hub.services;

import com.divecharter.hub.dto.BookingDtos.BookingRequest;
import com.divecharter.hub.dto.BookingDtos.BookingResponse;
import com.divecharter.hub.dto.CommonDtos.PageResponse;
import com.divecharter.hub.exceptions.BusinessRuleException;
import com.divecharter.hub.exceptions.CertificationInsufficientException;
import com.divecharter.hub.exceptions.CertificationNotVerifiedException;
import com.divecharter.hub.exceptions.DuplicateResourceException;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.Booking;
import com.divecharter.hub.models.DiveSite;
import com.divecharter.hub.models.DiveTrip;
import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.BookingStatus;
import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.models.enums.Role;
import com.divecharter.hub.models.enums.TripStatus;
import com.divecharter.hub.repositories.BookingRepository;
import com.divecharter.hub.repositories.DiveTripRepository;
import com.divecharter.hub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.divecharter.hub.dto.BookingDtos.ManifestEntry;
import com.divecharter.hub.dto.BookingDtos.ManifestResponse;
import java.util.List;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private static final int CANCELLATION_CUTOFF_HOURS = 24;

    private final BookingRepository bookingRepository;
    private final DiveTripRepository diveTripRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse create(Long userId, BookingRequest request) {
        User diver = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        DiveTrip trip = diveTripRepository.findByIdWithSite(request.tripId())
                .orElseThrow(() -> new ResourceNotFoundException("Dive trip", request.tripId()));
        DiveSite site = trip.getDiveSite();

        // Rule 1: certification must be verified by staff
        if (!diver.isCertVerified()) {
            throw new CertificationNotVerifiedException();
        }

        // Rule 2: certification must allow the site's depth
        CertificationLevel level = diver.getCertLevel();
        if (!level.canDive(site.getMaxDepthMeters())) {
            throw new CertificationInsufficientException(String.format(
                    "%s reaches %d m, but your %s certification only allows dives to %d m.",
                    site.getName(), site.getMaxDepthMeters(), level, level.getMaxDepthMeters()));
        }

        // Rule 3: trip must be scheduled and in the future
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BusinessRuleException("This trip is no longer open for booking");
        }
        if (!trip.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("This trip has already departed");
        }

        // Rule 4: no double-booking
        if (bookingRepository.existsByUserIdAndDiveTripIdAndStatus(
                diver.getId(), trip.getId(), BookingStatus.CONFIRMED)) {
            throw new DuplicateResourceException("You already have a booking on this trip");
        }

        // Rule 5: boat must have a free seat (throws TripFullException if not)
        trip.reserveSeat();

        Booking booking = new Booking();
        booking.setUser(diver);
        booking.setDiveTrip(trip);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(trip.getPrice());

        return toResponse(bookingRepository.save(booking));
    }

    public PageResponse<BookingResponse> findMyBookings(Long userId, Pageable pageable) {
        return PageResponse.from(
                bookingRepository.findByUserIdOrderByBookedAtDesc(userId, pageable).map(this::toResponse));
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, User currentUser) {
        Booking booking = bookingRepository.findWithDetailsById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only cancel your own bookings");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessRuleException("This booking is already cancelled");
        }

        DiveTrip trip = booking.getDiveTrip();
        LocalDateTime cutoff = trip.getDepartureTime().minusHours(CANCELLATION_CUTOFF_HOURS);
        if (!isAdmin && LocalDateTime.now().isAfter(cutoff)) {
            throw new BusinessRuleException("Bookings can only be cancelled at least "
                    + CANCELLATION_CUTOFF_HOURS + " hours before departure. Please contact the shop.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        trip.releaseSeat();
        return toResponse(booking);
    }
    public ManifestResponse getManifest(Long tripId) {
        DiveTrip trip = diveTripRepository.findByIdWithSite(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Dive trip", tripId));

        List<ManifestEntry> passengers = bookingRepository.findManifest(tripId, BookingStatus.CONFIRMED)
                .stream()
                .map(booking -> {
                    User diver = booking.getUser();
                    return new ManifestEntry(
                            booking.getId(),
                            diver.getFullName(),
                            diver.getEmail(),
                            diver.getCertLevel(),
                            diver.getCertAgency(),
                            diver.getCertNumber());
                })
                .toList();

        return new ManifestResponse(
                trip.getId(),
                trip.getDiveSite().getName(),
                trip.getBoatName(),
                trip.getDepartureTime(),
                trip.getReturnTime(),
                trip.getCapacity(),
                trip.getSeatsBooked(),
                passengers);
    }

    private BookingResponse toResponse(Booking booking) {
        DiveTrip trip = booking.getDiveTrip();
        return new BookingResponse(
                booking.getId(),
                trip.getId(),
                trip.getDiveSite().getName(),
                trip.getBoatName(),
                trip.getDepartureTime(),
                trip.getReturnTime(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getBookedAt());
    }
}