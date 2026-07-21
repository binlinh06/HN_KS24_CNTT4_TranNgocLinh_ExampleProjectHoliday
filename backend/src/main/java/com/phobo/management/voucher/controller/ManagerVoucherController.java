package com.phobo.management.voucher.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.voucher.dto.VoucherRequest;
import com.phobo.management.voucher.dto.VoucherResponse;
import com.phobo.management.voucher.service.VoucherManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager/vouchers")
@RequiredArgsConstructor
public class ManagerVoucherController {

    private final VoucherManagementService voucherManagementService;

    @GetMapping
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int validatedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<VoucherResponse> result = voucherManagementService.getVouchers(pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách mã giảm giá thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(@PathVariable String id) {
        VoucherResponse voucher = voucherManagementService.getVoucherById(id);
        return ResponseEntity.ok(ApiResponse.success(voucher, "Lấy chi tiết mã giảm giá thành công"));
    }

    @PostMapping
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(@Valid @RequestBody VoucherRequest request) {
        VoucherResponse response = voucherManagementService.createVoucher(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tạo khuyến mãi thành công"));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @PathVariable String id,
            @Valid @RequestBody VoucherRequest request) {
        VoucherResponse response = voucherManagementService.updateVoucher(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật khuyến mãi thành công"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Void>> activateVoucher(@PathVariable String id) {
        voucherManagementService.activateVoucher(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Kích hoạt mã giảm giá thành công"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Void>> deactivateVoucher(@PathVariable String id) {
        voucherManagementService.deactivateVoucher(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tắt mã giảm giá thành công"));
    }
}
