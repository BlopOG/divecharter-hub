package com.divecharter.hub.repositories;

import com.divecharter.hub.models.Booking;
import com.divecharter.hub.models.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByUserIdAndDiveTripIdAndStatus(Long userId, Long diveTripId, BookingStatus status);

    @EntityGraph(attributePaths = {"diveTrip", "diveTrip.diveSite"})
    Page<Booking> findByUserIdOrderByBookedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "diveTrip", "diveTrip.diveSite"})
    Optional<Booking> findWithDetailsById(Long id);
}