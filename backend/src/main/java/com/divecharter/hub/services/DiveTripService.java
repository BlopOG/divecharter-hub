package com.divecharter.hub.services;

import com.divecharter.hub.dto.DiveTripResponse;
import com.divecharter.hub.dto.PageResponse;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.DiveSite;
import com.divecharter.hub.models.DiveTrip;
import com.divecharter.hub.models.enums.TripStatus;
import com.divecharter.hub.repositories.DiveTripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiveTripService {

    private final DiveTripRepository diveTripRepository;

    public PageResponse<DiveTripResponse> findUpcoming(Pageable pageable) {
        return PageResponse.from(
                diveTripRepository.findUpcoming(TripStatus.SCHEDULED, LocalDateTime.now(), pageable)
                        .map(this::toResponse));
    }

    public DiveTripResponse findById(Long id) {
        DiveTrip trip = diveTripRepository.findByIdWithSite(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dive trip", id));
        return toResponse(trip);
    }

    private DiveTripResponse toResponse(DiveTrip trip) {
        DiveSite site = trip.getDiveSite();
        return new DiveTripResponse(
                trip.getId(),
                site.getId(),
                site.getName(),
                site.getLocation(),
                site.getMaxDepthMeters(),
                site.getDifficulty(),
                trip.getBoatName(),
                trip.getDepartureTime(),
                trip.getReturnTime(),
                trip.getCapacity(),
                trip.getSeatsBooked(),
                trip.getSeatsAvailable(),
                trip.getPrice(),
                trip.getStatus());
    }
}