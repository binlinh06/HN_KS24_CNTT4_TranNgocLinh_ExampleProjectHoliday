package com.phobo.management.product.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.common.dto.PageMetadata;
import com.phobo.management.product.dto.ProductRequest;
import com.phobo.management.product.dto.ProductResponse;
import com.phobo.management.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/api/v1/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        Page<ProductResponse> productPage = productService.getProducts(
                categoryId, keyword, isAvailable, isFeatured, minPrice, maxPrice, sort, page, size);

        PageMetadata meta = PageMetadata.builder()
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success(productPage.getContent(), meta, "Lấy danh sách món ăn thành công"));
    }

    @GetMapping("/api/v1/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Lấy chi tiết món ăn thành công"));
    }

    @GetMapping("/api/v1/products/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(@PathVariable String slug) {
        ProductResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product, "Lấy chi tiết món ăn theo slug thành công"));
    }

    @GetMapping("/api/v1/products/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        List<ProductResponse> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(ApiResponse.success(products, "Lấy danh sách món ăn nổi bật thành công"));
    }

    @PostMapping("/api/v1/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.success(product, "Dữ liệu đã được lưu thành công"));
    }

    @PutMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(product, "Dữ liệu đã được lưu thành công"));
    }

    @PatchMapping("/api/v1/admin/products/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleAvailability(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        Boolean isAvailable = body.get("isAvailable");
        if (isAvailable == null) {
            isAvailable = true;
        }
        ProductResponse product = productService.toggleAvailability(id, isAvailable);
        return ResponseEntity.ok(ApiResponse.success(product, "Cập nhật trạng thái thành công"));
    }

    @PatchMapping("/api/v1/admin/products/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleFeatured(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        Boolean isFeatured = body.get("isFeatured");
        if (isFeatured == null) {
            isFeatured = false;
        }
        ProductResponse product = productService.toggleFeatured(id, isFeatured);
        return ResponseEntity.ok(ApiResponse.success(product, "Cập nhật món nổi bật thành công"));
    }

    @DeleteMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa món ăn thành công"));
    }
}
