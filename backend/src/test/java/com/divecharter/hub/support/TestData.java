package com.divecharter.hub.support;

import com.divecharter.hub.models.Booking;
import com.divecharter.hub.models.DiveSite;
import com.divecharter.hub.models.DiveTrip;
import com.divecharter.hub.models.User;
import com.divecharter.hub.models.enums.BookingStatus;
import com.divecharter.hub.models.enums.CertificationLevel;
import com.divecharter.hub.models.enums.Difficulty;
import com.divecharter.hub.models.enums.Role;
import com.divecharter.hub.models.enums.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Builders for entities used across unit tests. */
public final class TestData {

    private TestData() {
    }

    public static User diver(Long id, CertificationLevel level, boolean verified) {
        User user = new User();
        user.setId(id);
        user.setEmail("diver" + id + "@test.com");
        user.setPasswordHash("hash");
        user.setFullName("Diver " + id);
        user.setRole(Role.USER);
        user.setCertLevel(level);
        user.setCertAgency("PADI");
        user.setCertVerified(verified);
        return user;
    }

    public static User admin(Long id) {
        User user = diver(id, CertificationLevel.DIVEMASTER, true);
        user.setRole(Role.ADMIN);
        return user;
    }

    public static DiveSite site(Long id, String name, int depth) {
        DiveSite site = new DiveSite();
        site.setId(id);
        site.setName(name);
        site.setLocation("Key Largo, Florida");
        site.setMaxDepthMeters(depth);
        site.setDifficulty(depth > 18 ? Difficulty.ADVANCED : Difficulty.BEGINNER);
        return site;
    }

    public static DiveTrip trip(Long id, DiveSite site, LocalDateTime departure, int capacity, int seatsBooked) {
        DiveTrip trip = new DiveTrip();
        trip.setId(id);
        trip.setDiveSite(site);
        trip.setBoatName("Reef Runner");
        trip.setDepartureTime(departure);
        trip.setReturnTime(departure.plusHours(4));
        trip.setCapacity(capacity);
        trip.setSeatsBooked(seatsBooked);
        trip.setPrice(new BigDecimal("120.00"));
        trip.setStatus(TripStatus.SCHEDULED);
        return trip;
    }

    public static Booking booking(Long id, User user, DiveTrip trip, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setUser(user);
        booking.setDiveTrip(trip);
        booking.setStatus(status);
        booking.setTotalPrice(trip.getPrice());
        return booking;
    }
}