package com.phobo.management.category.controller;

import com.phobo.management.category.dto.CategoryRequest;
import com.phobo.management.category.dto.CategoryResponse;
import com.phobo.management.category.service.CategoryService;
import com.phobo.management.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/v1/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getPublicCategories() {
        List<CategoryResponse> categories = categoryService.getPublicCategories();
        return ResponseEntity.ok(ApiResponse.success(categories, "Lấy danh sách danh mục thành công"));
    }

    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable String id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category, "Lấy chi tiết danh mục thành công"));
    }

    @GetMapping("/api/v1/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories, "Lấy tất cả danh mục thành công (Admin)"));
    }

    @PostMapping("/api/v1/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success(category, "Dữ liệu đã được lưu thành công"));
    }

    @PutMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(category, "Dữ liệu đã được lưu thành công"));
    }

    @PatchMapping("/api/v1/admin/categories/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> toggleStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        Boolean isActive = body.get("isActive");
        if (isActive == null) {
            isActive = true;
        }
        CategoryResponse category = categoryService.toggleStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(category, "Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa danh mục thành công"));
    }
}
