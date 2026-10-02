package com.divecharter.hub.services;

import com.divecharter.hub.dto.DiveSiteRequest;
import com.divecharter.hub.dto.DiveSiteResponse;
import com.divecharter.hub.exceptions.DuplicateResourceException;
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

    @Transactional
    public DiveSiteResponse create(DiveSiteRequest request) {
        String name = request.name().trim();
        if (diveSiteRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A dive site named '" + name + "' already exists");
        }
        DiveSite site = new DiveSite();
        applyRequest(request, site);
        return toResponse(diveSiteRepository.save(site));
    }

    @Transactional
    public DiveSiteResponse update(Long id, DiveSiteRequest request) {
        DiveSite site = diveSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dive site", id));
        String name = request.name().trim();
        if (diveSiteRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("A dive site named '" + name + "' already exists");
        }
        applyRequest(request, site);
        return toResponse(site);
    }

    @Transactional
    public void delete(Long id) {
        DiveSite site = diveSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dive site", id));
        diveSiteRepository.delete(site);
    }

    private void applyRequest(DiveSiteRequest request, DiveSite site) {
        site.setName(request.name().trim());
        site.setLocation(request.location().trim());
        site.setMaxDepthMeters(request.maxDepthMeters());
        site.setDifficulty(request.difficulty());
        site.setDescription(request.description() == null ? null : request.description().trim());
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