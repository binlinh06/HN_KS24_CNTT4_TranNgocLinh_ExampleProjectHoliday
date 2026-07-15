package com.phobo.management.checkout.controller;

import com.phobo.management.checkout.dto.CheckoutPreviewRequest;
import com.phobo.management.checkout.dto.CheckoutPreviewResponse;
import com.phobo.management.checkout.service.CheckoutService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<CheckoutPreviewResponse>> previewCheckout(
            @Validated @RequestBody CheckoutPreviewRequest request) {
        CheckoutPreviewResponse response = checkoutService.previewCheckout(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Khởi tạo xem trước thanh toán thành công"));
    }
}
