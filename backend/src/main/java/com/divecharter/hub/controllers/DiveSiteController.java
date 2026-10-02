package com.divecharter.hub.controllers;

import com.divecharter.hub.dto.DiveSiteRequest;
import com.divecharter.hub.dto.DiveSiteResponse;
import com.divecharter.hub.services.DiveSiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;



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
    @PostMapping
    public ResponseEntity<DiveSiteResponse> create(@Valid @RequestBody DiveSiteRequest request) {
        DiveSiteResponse created = diveSiteService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public DiveSiteResponse update(@PathVariable Long id, @Valid @RequestBody DiveSiteRequest request) {
        return diveSiteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        diveSiteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}