package com.phobo.management.product.controller;

import com.phobo.management.product.service.ProductService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Product.
 * Relevant Use Cases: UC-05, UC-23
 */
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-05, UC-23 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(productService.getInfo(), "Skeleton active"));
    }
}
