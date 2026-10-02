package com.divecharter.hub.services;

import com.divecharter.hub.dto.DiveSiteResponse;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.DiveSite;
import com.divecharter.hub.repositories.DiveSiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiveSiteService {

    private final DiveSiteRepository diveSiteRepository;

    public List<DiveSiteResponse> findAll() {
        return diveSiteRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public DiveSiteResponse findById(Long id) {
        DiveSite site = diveSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dive site", id));
        return toResponse(site);
    }

    private DiveSiteResponse toResponse(DiveSite site) {
        return new DiveSiteResponse(
                site.getId(),
                site.getName(),
                site.getLocation(),
                site.getMaxDepthMeters(),
                site.getDifficulty(),
                site.getDescription());
    }
}