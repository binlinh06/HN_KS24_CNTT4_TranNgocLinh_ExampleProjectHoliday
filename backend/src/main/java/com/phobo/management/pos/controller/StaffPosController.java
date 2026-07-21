package com.phobo.management.pos.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.pos.dto.PosOrderRequest;
import com.phobo.management.pos.dto.PosOrderResponse;
import com.phobo.management.pos.dto.PosPaymentRequest;
import com.phobo.management.pos.service.PosOrderService;
import com.phobo.management.pos.service.PosPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/pos")
@RequiredArgsConstructor
@Slf4j
public class StaffPosController {

    private final PosOrderService posOrderService;
    private final PosPaymentService posPaymentService;

    @PostMapping("/preview")
    @PreAuthorize("@employeeAuthService.isCashier()")
    public ResponseEntity<ApiResponse<PosOrderResponse>> previewOrder(@RequestBody PosOrderRequest request) {
        PosOrderResponse response = posOrderService.previewOrder(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tính toán báo giá POS thành công"));
    }

    @PostMapping("/orders")
    @PreAuthorize("@employeeAuthService.isCashier()")
    public ResponseEntity<ApiResponse<PosOrderResponse>> createOrder(
            @RequestBody PosOrderRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        log.info("Creating POS counter order. Idempotency-Key: {}", idempotencyKey);
        PosOrderResponse response = posOrderService.createOrder(request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success(response, "Tạo đơn hàng POS thành công"));
    }

    @PostMapping("/orders/{orderId}/payments")
    @PreAuthorize("@employeeAuthService.isCashier()")
    public ResponseEntity<ApiResponse<PosOrderResponse>> processPayment(
            @PathVariable String orderId,
            @RequestBody PosPaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        log.info("Processing POS payment for order {}. Idempotency-Key: {}", orderId, idempotencyKey);
        PosOrderResponse response = posPaymentService.processPosPayment(orderId, request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success(response, "Thanh toán hóa đơn POS thành công"));
    }
}
