package com.phobo.management.kitchen.controller;

import com.phobo.management.kitchen.service.KitchenService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Kitchen.
 * Relevant Use Cases: UC-20
 */
@RestController
@RequestMapping("/api/v1/kitchen")
public class KitchenController {

    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-20 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(kitchenService.getInfo(), "Skeleton active"));
    }
}
