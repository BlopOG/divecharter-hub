package com.divecharter.hub.services;

import com.divecharter.hub.dto.DiveSiteRequest;
import com.divecharter.hub.dto.DiveSiteResponse;
import com.divecharter.hub.exceptions.DuplicateResourceException;
import com.divecharter.hub.exceptions.ResourceNotFoundException;
import com.divecharter.hub.models.DiveSite;
import com.divecharter.hub.repositories.DiveSiteRepository;
import com.divecharter.hub.utils.InputSanitizer;
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
        String name = InputSanitizer.clean(request.name());
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
        String name = InputSanitizer.clean(request.name());
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
        site.setName(InputSanitizer.clean(request.name()));
        site.setLocation(InputSanitizer.clean(request.location()));
        site.setMaxDepthMeters(request.maxDepthMeters());
        site.setDifficulty(request.difficulty());
        site.setDescription(InputSanitizer.clean(request.description()));
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