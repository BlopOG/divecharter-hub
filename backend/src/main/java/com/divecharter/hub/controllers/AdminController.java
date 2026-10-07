package com.divecharter.hub.controllers;

import com.divecharter.hub.dto.UserDtos.CertificationVerificationRequest;
import com.divecharter.hub.dto.BookingDtos.ManifestResponse;
import com.divecharter.hub.dto.CommonDtos.PageResponse;
import com.divecharter.hub.dto.UserDtos.UserResponse;
import com.divecharter.hub.services.BookingService;
import com.divecharter.hub.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final BookingService bookingService;

    @GetMapping("/users")
    public PageResponse<UserResponse> listDivers(
            @RequestParam(defaultValue = "false") boolean pendingOnly,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return userService.listDivers(pendingOnly, pageable);
    }

    @PatchMapping("/users/{id}/certification")
    public UserResponse verifyCertification(@PathVariable Long id,
                                            @Valid @RequestBody CertificationVerificationRequest request) {
        return userService.setCertificationVerified(id, request.verified());
    }

    @GetMapping("/trips/{id}/manifest")
    public ManifestResponse manifest(@PathVariable Long id) {
        return bookingService.getManifest(id);
    }
}