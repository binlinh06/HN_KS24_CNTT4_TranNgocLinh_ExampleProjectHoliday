package com.phobo.management.systemconfig.controller;

import com.phobo.management.systemconfig.service.SystemconfigService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Systemconfig.
 * Relevant Use Cases: UC-26
 */
@RestController
@RequestMapping("/api/v1/systemconfig")
public class SystemconfigController {

    private final SystemconfigService systemconfigService;

    public SystemconfigController(SystemconfigService systemconfigService) {
        this.systemconfigService = systemconfigService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-26 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(systemconfigService.getInfo(), "Skeleton active"));
    }
}
