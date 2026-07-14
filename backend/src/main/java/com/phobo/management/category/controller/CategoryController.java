package com.phobo.management.category.controller;

import com.phobo.management.category.service.CategoryService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Category.
 * Relevant Use Cases: UC-22
 */
@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-22 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(categoryService.getInfo(), "Skeleton active"));
    }
}
