package com.divecharter.hub.controllers;

import com.divecharter.hub.dto.BookingRequest;
import com.divecharter.hub.dto.BookingResponse;
import com.divecharter.hub.dto.PageResponse;
import com.divecharter.hub.models.User;
import com.divecharter.hub.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(@AuthenticationPrincipal User currentUser,
                                                  @Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.create(currentUser.getId(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/me")
    public PageResponse<BookingResponse> myBookings(@AuthenticationPrincipal User currentUser,
                                                    @PageableDefault(size = 10) Pageable pageable) {
        return bookingService.findMyBookings(currentUser.getId(), pageable);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return bookingService.cancel(id, currentUser);
    }
}