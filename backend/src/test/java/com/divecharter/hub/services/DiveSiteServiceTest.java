package com.divecharter.hub.services;

import com.divecharter.hub.dto.DiveDtos.DiveSiteRequest;
import com.divecharter.hub.dto.DiveDtos.DiveSiteResponse;
import com.divecharter.hub.exceptions.DuplicateResourceException;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.DiveSite;
import com.divecharter.hub.models.enums.Difficulty;
import com.divecharter.hub.repositories.DiveSiteRepository;
import com.divecharter.hub.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiveSiteServiceTest {

    @Mock private DiveSiteRepository diveSiteRepository;

    @InjectMocks private DiveSiteService diveSiteService;

    private DiveSiteRequest request(String name) {
        return new DiveSiteRequest(name, "Practice Bay", 10, Difficulty.BEGINNER, "A test site");
    }

    @Test
    void findAll_mapsEverySite() {
        when(diveSiteRepository.findAll()).thenReturn(List.of(
                TestData.site(1L, "Reef A", 10), TestData.site(2L, "Reef B", 20)));

        List<DiveSiteResponse> result = diveSiteService.findAll();

        assertThat(result).extracting(DiveSiteResponse::name).containsExactly("Reef A", "Reef B");
    }

    @Test
    void findById_throwsWhenMissing() {
        when(diveSiteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diveSiteService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesSanitizedSite() {
        when(diveSiteRepository.save(any(DiveSite.class))).thenAnswer(inv -> inv.getArgument(0));

        DiveSiteResponse response = diveSiteService.create(request("<b>Test Reef</b>"));

        assertThat(response.name()).isEqualTo("Test Reef");
        assertThat(response.maxDepthMeters()).isEqualTo(10);
    }

    @Test
    void create_rejectsDuplicateName() {
        when(diveSiteRepository.existsByNameIgnoreCase("Test Reef")).thenReturn(true);

        assertThatThrownBy(() -> diveSiteService.create(request("Test Reef")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(diveSiteRepository, never()).save(any());
    }

    @Test
    void update_changesExistingSite() {
        DiveSite existing = TestData.site(5L, "Old Name", 12);
        when(diveSiteRepository.findById(5L)).thenReturn(Optional.of(existing));

        DiveSiteResponse response = diveSiteService.update(5L, request("New Name"));

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(existing.getName()).isEqualTo("New Name");
    }

    @Test
    void delete_throwsWhenMissing() {
        when(diveSiteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diveSiteService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}