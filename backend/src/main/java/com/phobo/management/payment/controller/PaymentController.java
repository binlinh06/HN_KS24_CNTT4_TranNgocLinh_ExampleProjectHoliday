package com.phobo.management.payment.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.payment.dto.PaymentResponse;
import com.phobo.management.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}/initiate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @PathVariable String orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        PaymentResponse response = paymentService.initiatePayment(orderId, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success(response, "Khởi tạo thanh toán thành công"));
    }

    @PostMapping("/orders/{orderId}/retry")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> retryPayment(
            @PathVariable String orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        PaymentResponse response = paymentService.initiatePayment(orderId, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success(response, "Thử lại thanh toán thành công"));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentDetails(@PathVariable String orderId) {
        PaymentResponse response = paymentService.getPaymentDetails(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin thanh toán thành công"));
    }

    @GetMapping("/return/{provider}")
    public ResponseEntity<ApiResponse<PaymentResponse>> returnCallback(
            @PathVariable String provider,
            @RequestParam Map<String, String> params) {
        PaymentResponse response = paymentService.processCallback(provider, params);
        return ResponseEntity.ok(ApiResponse.success(response, "Xử lý kết quả thanh toán từ redirect thành công"));
    }

    @PostMapping("/webhook/{provider}")
    public ResponseEntity<ApiResponse<PaymentResponse>> webhookCallback(
            @PathVariable String provider,
            @RequestParam Map<String, String> params) {
        PaymentResponse response = paymentService.processCallback(provider, params);
        return ResponseEntity.ok(ApiResponse.success(response, "Xử lý kết quả thanh toán từ webhook thành công"));
    }
}
