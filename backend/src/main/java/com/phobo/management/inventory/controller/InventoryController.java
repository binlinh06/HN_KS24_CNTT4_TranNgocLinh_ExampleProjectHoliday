package com.phobo.management.inventory.controller;

import com.phobo.management.inventory.service.InventoryService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Inventory.
 * Relevant Use Cases: UC-29
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-29 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInfo(), "Skeleton active"));
    }
}
