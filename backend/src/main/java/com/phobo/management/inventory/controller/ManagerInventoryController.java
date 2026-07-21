package com.phobo.management.inventory.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.inventory.dto.*;
import com.phobo.management.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/inventory")
@RequiredArgsConstructor
public class ManagerInventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/ingredients")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Page<IngredientResponse>>> getIngredients(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int validatedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.ASC, "name"));
        Page<IngredientResponse> result = inventoryService.getIngredients(keyword, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách nguyên liệu thành công"));
    }

    @GetMapping("/ingredients/low-stock")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<List<IngredientResponse>>> getLowStockIngredients() {
        List<IngredientResponse> list = inventoryService.getLowStockIngredients();
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách nguyên liệu sắp hết thành công"));
    }

    @PostMapping("/ingredients")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<IngredientResponse>> createIngredient(@Valid @RequestBody IngredientRequest request) {
        IngredientResponse response = inventoryService.createIngredient(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tạo nguyên liệu mới thành công"));
    }

    @PatchMapping("/ingredients/{id}")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<IngredientResponse>> updateIngredient(
            @PathVariable String id,
            @Valid @RequestBody IngredientRequest request) {
        IngredientResponse response = inventoryService.updateIngredient(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật thông tin nguyên liệu thành công"));
    }

    @PostMapping("/adjust")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> processStockAdjustment(
            @Valid @RequestBody InventoryStockAdjustmentRequest request) {
        InventoryTransactionResponse response = inventoryService.processStockAdjustment(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Thực hiện giao dịch kho thành công"));
    }

    @GetMapping("/transactions")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Page<InventoryTransactionResponse>>> getTransactions(
            @RequestParam(required = false) String ingredientId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int validatedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<InventoryTransactionResponse> result = inventoryService.getTransactions(ingredientId, transactionType, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy lịch sử giao dịch kho thành công"));
    }
}
