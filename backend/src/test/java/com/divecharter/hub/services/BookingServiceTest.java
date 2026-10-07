package com.divecharter.hub.services;

import com.divecharter.hub.dto.BookingDtos.BookingRequest;
import com.divecharter.hub.dto.BookingDtos.BookingResponse;
import com.divecharter.hub.dto.BookingDtos.ManifestResponse;
import com.divecharter.hub.exceptions.BusinessRuleException;
import com.divecharter.hub.exceptions.CertificationInsufficientException;
import com.divecharter.hub.exceptions.CertificationNotVerifiedException;
import com.divecharter.hub.exceptions.DuplicateResourceException;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.exceptions.TripFullException;
import com.divecharter.hub.models.Booking;
import com.divecharter.hub.models.DiveSite;
import com.divecharter.hub.models.DiveTrip;
import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.BookingStatus;
import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.models.enums.TripStatus;
import com.divecharter.hub.repositories.BookingRepository;
import com.divecharter.hub.repositories.DiveTripRepository;
import com.divecharter.hub.repositories.UserRepository;
import com.divecharter.hub.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private DiveTripRepository diveTripRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BookingService bookingService;

    private User diver;
    private DiveSite reef;
    private DiveTrip reefTrip;

    @BeforeEach
    void setUp() {
        diver = TestData.diver(1L, CertificationLevel.ADVANCED_OPEN_WATER, true);
        reef = TestData.site(10L, "Molasses Reef", 12);
        reefTrip = TestData.trip(100L, reef, LocalDateTime.now().plusDays(5), 12, 0);
    }

    private void givenDiverAndTrip(User user, DiveTrip trip) {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(diveTripRepository.findByIdWithSite(trip.getId())).thenReturn(Optional.of(trip));
    }

    // ---------- create ----------

    @Test
    void create_succeeds_reservesSeatAndChargesTripPrice() {
        givenDiverAndTrip(diver, reefTrip);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.create(1L, new BookingRequest(100L));

        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.totalPrice()).isEqualByComparingTo("120.00");
        assertThat(response.siteName()).isEqualTo("Molasses Reef");
        assertThat(reefTrip.getSeatsBooked()).isEqualTo(1);
    }

    @Test
    void create_rejectsUnverifiedDiver() {
        User unverified = TestData.diver(2L, CertificationLevel.ADVANCED_OPEN_WATER, false);
        givenDiverAndTrip(unverified, reefTrip);

        assertThatThrownBy(() -> bookingService.create(2L, new BookingRequest(100L)))
                .isInstanceOf(CertificationNotVerifiedException.class);
        verify(bookingRepository, never()).save(any());
        assertThat(reefTrip.getSeatsBooked()).isZero();
    }

    @Test
    void create_rejectsSiteDeeperThanCertification() {
        User openWater = TestData.diver(3L, CertificationLevel.OPEN_WATER, true);
        DiveTrip deepTrip = TestData.trip(200L, TestData.site(20L, "Great Blue Hole", 40),
                LocalDateTime.now().plusDays(5), 8, 0);
        givenDiverAndTrip(openWater, deepTrip);

        assertThatThrownBy(() -> bookingService.create(3L, new BookingRequest(200L)))
                .isInstanceOf(CertificationInsufficientException.class)
                .hasMessageContaining("Great Blue Hole")
                .hasMessageContaining("18 m");
        assertThat(deepTrip.getSeatsBooked()).isZero();
    }

    @Test
    void create_rejectsCancelledTrip() {
        reefTrip.setStatus(TripStatus.CANCELLED);
        givenDiverAndTrip(diver, reefTrip);

        assertThatThrownBy(() -> bookingService.create(1L, new BookingRequest(100L)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void create_rejectsTripThatAlreadyDeparted() {
        DiveTrip pastTrip = TestData.trip(300L, reef, LocalDateTime.now().minusHours(1), 12, 0);
        givenDiverAndTrip(diver, pastTrip);

        assertThatThrownBy(() -> bookingService.create(1L, new BookingRequest(300L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("departed");
    }

    @Test
    void create_rejectsDoubleBooking() {
        givenDiverAndTrip(diver, reefTrip);
        when(bookingRepository.existsByUserIdAndDiveTripIdAndStatus(1L, 100L, BookingStatus.CONFIRMED))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(1L, new BookingRequest(100L)))
                .isInstanceOf(DuplicateResourceException.class);
        assertThat(reefTrip.getSeatsBooked()).isZero();
    }

    @Test
    void create_rejectsFullTrip() {
        DiveTrip fullTrip = TestData.trip(400L, reef, LocalDateTime.now().plusDays(2), 2, 2);
        givenDiverAndTrip(diver, fullTrip);

        assertThatThrownBy(() -> bookingService.create(1L, new BookingRequest(400L)))
                .isInstanceOf(TripFullException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_throwsWhenTripNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(diver));
        when(diveTripRepository.findByIdWithSite(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(1L, new BookingRequest(999L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- cancel ----------

    @Test
    void cancel_byOwnerBeforeCutoff_releasesSeat() {
        reefTrip.setSeatsBooked(1);
        Booking booking = TestData.booking(50L, diver, reefTrip, BookingStatus.CONFIRMED);
        when(bookingRepository.findWithDetailsById(50L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.cancel(50L, diver);

        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(reefTrip.getSeatsBooked()).isZero();
    }

    @Test
    void cancel_byAnotherDiver_isDenied() {
        Booking booking = TestData.booking(50L, diver, reefTrip, BookingStatus.CONFIRMED);
        when(bookingRepository.findWithDetailsById(50L)).thenReturn(Optional.of(booking));
        User stranger = TestData.diver(9L, CertificationLevel.OPEN_WATER, true);

        assertThatThrownBy(() -> bookingService.cancel(50L, stranger))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cancel_insideCutoff_isRejectedForDiver_butAllowedForAdmin() {
        DiveTrip soonTrip = TestData.trip(500L, reef, LocalDateTime.now().plusHours(10), 12, 1);
        Booking booking = TestData.booking(60L, diver, soonTrip, BookingStatus.CONFIRMED);
        when(bookingRepository.findWithDetailsById(60L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(60L, diver))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("24 hours");

        BookingResponse response = bookingService.cancel(60L, TestData.admin(77L));
        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(soonTrip.getSeatsBooked()).isZero();
    }

    @Test
    void cancel_alreadyCancelled_isRejected() {
        Booking booking = TestData.booking(70L, diver, reefTrip, BookingStatus.CANCELLED);
        when(bookingRepository.findWithDetailsById(70L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(70L, diver))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already cancelled");
    }

    // ---------- my bookings & manifest ----------

    @Test
    void getManifest_listsConfirmedPassengers() {
        Booking booking = TestData.booking(80L, diver, reefTrip, BookingStatus.CONFIRMED);
        when(diveTripRepository.findByIdWithSite(100L)).thenReturn(Optional.of(reefTrip));
        when(bookingRepository.findManifest(100L, BookingStatus.CONFIRMED)).thenReturn(List.of(booking));

        ManifestResponse manifest = bookingService.getManifest(100L);

        assertThat(manifest.siteName()).isEqualTo("Molasses Reef");
        assertThat(manifest.passengers()).hasSize(1);
        assertThat(manifest.passengers().get(0).certLevel()).isEqualTo(CertificationLevel.ADVANCED_OPEN_WATER);
    }

    @Test
    void getManifest_throwsWhenTripNotFound() {
        when(diveTripRepository.findByIdWithSite(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getManifest(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}