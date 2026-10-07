package com.divecharter.hub.services;

import com.divecharter.hub.dto.DiveDtos.DiveTripResponse;
import com.divecharter.hub.dto.CommonDtos.PageResponse;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.DiveTrip;
import com.divecharter.hub.models.enums.TripStatus;
import com.divecharter.hub.repositories.DiveTripRepository;
import com.divecharter.hub.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiveTripServiceTest {

    @Mock private DiveTripRepository diveTripRepository;

    @InjectMocks private DiveTripService diveTripService;

    private final DiveTrip trip = TestData.trip(1L, TestData.site(10L, "Molasses Reef", 12),
            LocalDateTime.now().plusDays(3), 12, 4);

    @Test
    void findUpcoming_mapsTripsWithSiteDetails() {
        Pageable pageable = PageRequest.of(0, 10);
        when(diveTripRepository.findUpcoming(eq(TripStatus.SCHEDULED), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(trip), pageable, 1));

        PageResponse<DiveTripResponse> result = diveTripService.findUpcoming(pageable);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content().get(0).siteName()).isEqualTo("Molasses Reef");
        assertThat(result.content().get(0).seatsAvailable()).isEqualTo(8);
    }

    @Test
    void findById_throwsWhenMissing() {
        when(diveTripRepository.findByIdWithSite(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diveTripService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}