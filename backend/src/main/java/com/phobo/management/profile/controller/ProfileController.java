package com.phobo.management.profile.controller;

import com.phobo.management.profile.service.ProfileService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Profile.
 * Relevant Use Cases: UC-03
 */
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-03 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(profileService.getInfo(), "Skeleton active"));
    }
}
