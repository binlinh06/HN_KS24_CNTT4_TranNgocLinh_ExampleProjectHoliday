package com.phobo.management.voucher.controller;

import com.phobo.management.voucher.service.VoucherService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Voucher.
 * Relevant Use Cases: UC-08, UC-24
 */
@RestController
@RequestMapping("/api/v1/voucher")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-08, UC-24 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(voucherService.getInfo(), "Skeleton active"));
    }
}
