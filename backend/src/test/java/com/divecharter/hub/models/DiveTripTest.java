package com.divecharter.hub.models;

import com.divecharter.hub.exceptions.TripFullException;
import com.divecharter.hub.support.TestData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiveTripTest {

    private DiveTrip tripWith(int capacity, int seatsBooked) {
        return TestData.trip(1L, TestData.site(1L, "Reef", 12),
                LocalDateTime.now().plusDays(3), capacity, seatsBooked);
    }

    @Test
    void reserveSeat_takesOneSeat() {
        DiveTrip trip = tripWith(10, 3);

        trip.reserveSeat();

        assertThat(trip.getSeatsBooked()).isEqualTo(4);
        assertThat(trip.getSeatsAvailable()).isEqualTo(6);
    }

    @Test
    void reserveSeat_throwsWhenFull() {
        DiveTrip trip = tripWith(2, 2);

        assertThat(trip.isFull()).isTrue();
        assertThatThrownBy(trip::reserveSeat).isInstanceOf(TripFullException.class);
        assertThat(trip.getSeatsBooked()).isEqualTo(2);
    }

    @Test
    void releaseSeat_neverGoesBelowZero() {
        DiveTrip trip = tripWith(10, 1);

        trip.releaseSeat();
        trip.releaseSeat();

        assertThat(trip.getSeatsBooked()).isZero();
    }
}