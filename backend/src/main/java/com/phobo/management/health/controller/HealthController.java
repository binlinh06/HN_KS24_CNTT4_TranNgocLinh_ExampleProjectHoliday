package com.phobo.management.health.controller;

import com.phobo.management.health.service.HealthService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> check() {
        return ResponseEntity.ok(ApiResponse.success(healthService.checkHealth(), "Hệ thống hoạt động bình thường"));
    }
}
