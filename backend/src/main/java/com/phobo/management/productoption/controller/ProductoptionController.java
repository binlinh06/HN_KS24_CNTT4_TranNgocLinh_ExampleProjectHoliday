package com.phobo.management.productoption.controller;

import com.phobo.management.productoption.service.ProductoptionService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Productoption.
 * Relevant Use Cases: UC-06
 */
@RestController
@RequestMapping("/api/v1/productoption")
public class ProductoptionController {

    private final ProductoptionService productoptionService;

    public ProductoptionController(ProductoptionService productoptionService) {
        this.productoptionService = productoptionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-06 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(productoptionService.getInfo(), "Skeleton active"));
    }
}
