package com.phobo.management.restauranttable.controller;

import com.phobo.management.restauranttable.service.RestauranttableService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Restauranttable.
 * Relevant Use Cases: UC-16
 */
@RestController
@RequestMapping("/api/v1/restauranttable")
public class RestauranttableController {

    private final RestauranttableService restauranttableService;

    public RestauranttableController(RestauranttableService restauranttableService) {
        this.restauranttableService = restauranttableService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-16 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(restauranttableService.getInfo(), "Skeleton active"));
    }
}
