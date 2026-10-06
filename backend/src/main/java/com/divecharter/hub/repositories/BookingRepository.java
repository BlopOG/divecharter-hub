package com.divecharter.hub.repositories;

import com.divecharter.hub.models.Booking;
import com.divecharter.hub.models.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByUserIdAndDiveTripIdAndStatus(Long userId, Long diveTripId, BookingStatus status);

    @EntityGraph(attributePaths = {"diveTrip", "diveTrip.diveSite"})
    Page<Booking> findByUserIdOrderByBookedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "diveTrip", "diveTrip.diveSite"})
    Optional<Booking> findWithDetailsById(Long id);
    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.user u
            WHERE b.diveTrip.id = :tripId AND b.status = :status
            ORDER BY u.fullName
            """)
    List<Booking> findManifest(@Param("tripId") Long tripId, @Param("status") BookingStatus status);
}