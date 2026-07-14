package com.phobo.management.productoption.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.productoption.dto.OptionGroupRequest;
import com.phobo.management.productoption.dto.OptionGroupResponse;
import com.phobo.management.productoption.dto.OptionRequest;
import com.phobo.management.productoption.dto.OptionResponse;
import com.phobo.management.productoption.service.ProductOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    @GetMapping("/api/v1/products/{productId}/options")
    public ResponseEntity<ApiResponse<List<OptionGroupResponse>>> getProductOptions(@PathVariable String productId) {
        List<OptionGroupResponse> options = productOptionService.getProductOptions(productId);
        return ResponseEntity.ok(ApiResponse.success(options, "Lấy danh sách tùy chọn của món ăn thành công"));
    }

    @GetMapping("/api/v1/admin/option-groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OptionGroupResponse>>> getAllOptionGroups() {
        List<OptionGroupResponse> groups = productOptionService.getAllOptionGroups();
        return ResponseEntity.ok(ApiResponse.success(groups, "Lấy danh sách nhóm tùy chọn thành công (Admin)"));
    }

    @PostMapping("/api/v1/admin/option-groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OptionGroupResponse>> createOptionGroup(@Valid @RequestBody OptionGroupRequest request) {
        OptionGroupResponse group = productOptionService.createOptionGroup(request);
        return ResponseEntity.ok(ApiResponse.success(group, "Dữ liệu đã được lưu thành công"));
    }

    @PutMapping("/api/v1/admin/option-groups/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OptionGroupResponse>> updateOptionGroup(
            @PathVariable String id,
            @Valid @RequestBody OptionGroupRequest request) {
        OptionGroupResponse group = productOptionService.updateOptionGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success(group, "Dữ liệu đã được lưu thành công"));
    }

    @DeleteMapping("/api/v1/admin/option-groups/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOptionGroup(@PathVariable String id) {
        productOptionService.deleteOptionGroup(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa nhóm tùy chọn thành công"));
    }

    @PostMapping("/api/v1/admin/option-groups/{groupId}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OptionResponse>> addOption(
            @PathVariable String groupId,
            @Valid @RequestBody OptionRequest request) {
        OptionResponse option = productOptionService.addOption(groupId, request);
        return ResponseEntity.ok(ApiResponse.success(option, "Dữ liệu đã được lưu thành công"));
    }

    @PutMapping("/api/v1/admin/options/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OptionResponse>> updateOption(
            @PathVariable String optionId,
            @Valid @RequestBody OptionRequest request) {
        OptionResponse option = productOptionService.updateOption(optionId, request);
        return ResponseEntity.ok(ApiResponse.success(option, "Dữ liệu đã được lưu thành công"));
    }

    @DeleteMapping("/api/v1/admin/options/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOption(@PathVariable String optionId) {
        productOptionService.deleteOption(optionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa tùy chọn thành công"));
    }

    @PostMapping("/api/v1/admin/products/{productId}/option-groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignGroupToProduct(
            @PathVariable String productId,
            @RequestBody Map<String, String> body) {
        String groupId = body.get("groupId");
        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.success(null, "groupId không được trống"));
        }
        productOptionService.assignGroupToProduct(productId, groupId);
        return ResponseEntity.ok(ApiResponse.success(null, "Gán nhóm tùy chọn thành công"));
    }

    @DeleteMapping("/api/v1/admin/products/{productId}/option-groups/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeGroupFromProduct(
            @PathVariable String productId,
            @PathVariable String groupId) {
        productOptionService.removeGroupFromProduct(productId, groupId);
        return ResponseEntity.ok(ApiResponse.success(null, "Hủy gán nhóm tùy chọn thành công"));
    }
}
