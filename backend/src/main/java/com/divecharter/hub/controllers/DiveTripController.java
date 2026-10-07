package com.divecharter.hub.controllers;

import com.divecharter.hub.dto.DiveDtos.DiveTripResponse;
import com.divecharter.hub.dto.CommonDtos.PageResponse;
import com.divecharter.hub.services.DiveTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class DiveTripController {

    private final DiveTripService diveTripService;

    @GetMapping
    public PageResponse<DiveTripResponse> getUpcoming(
            @PageableDefault(size = 10, sort = "departureTime") Pageable pageable) {
        return diveTripService.findUpcoming(pageable);
    }

    @GetMapping("/{id}")
    public DiveTripResponse getById(@PathVariable Long id) {
        return diveTripService.findById(id);
    }
}