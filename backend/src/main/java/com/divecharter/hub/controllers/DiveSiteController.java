package com.divecharter.hub.controllers;

import com.divecharter.hub.dto.DiveSiteResponse;
import com.divecharter.hub.services.DiveSiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dive-sites")
@RequiredArgsConstructor
public class DiveSiteController {

    private final DiveSiteService diveSiteService;

    @GetMapping
    public List<DiveSiteResponse> getAll() {
        return diveSiteService.findAll();
    }

    @GetMapping("/{id}")
    public DiveSiteResponse getById(@PathVariable Long id) {
        return diveSiteService.findById(id);
    }
}