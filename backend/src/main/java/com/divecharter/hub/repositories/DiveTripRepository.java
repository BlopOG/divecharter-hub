package com.divecharter.hub.repositories;

import com.divecharter.hub.models.DiveTrip;
import com.divecharter.hub.models.enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DiveTripRepository extends JpaRepository<DiveTrip, Long> {

    @Query(value = """
            SELECT t FROM DiveTrip t JOIN FETCH t.diveSite
            WHERE t.status = :status AND t.departureTime > :now
            """,
            countQuery = """
            SELECT COUNT(t) FROM DiveTrip t
            WHERE t.status = :status AND t.departureTime > :now
            """)
    Page<DiveTrip> findUpcoming(@Param("status") TripStatus status,
                                @Param("now") LocalDateTime now,
                                Pageable pageable);

    @Query("SELECT t FROM DiveTrip t JOIN FETCH t.diveSite WHERE t.id = :id")
    Optional<DiveTrip> findByIdWithSite(@Param("id") Long id);
}