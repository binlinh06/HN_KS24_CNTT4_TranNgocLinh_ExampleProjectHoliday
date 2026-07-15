package com.phobo.management.payment.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.payment.dto.PaymentResponse;
import com.phobo.management.payment.service.PaymentService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dev/mock-payments")
@Profile({"dev", "test"})
public class DevPaymentSimulationController {

    private final PaymentService paymentService;

    public DevPaymentSimulationController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{paymentId}/simulate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> simulateMockPayment(
            @PathVariable String paymentId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        PaymentResponse response = paymentService.simulateMockPayment(paymentId, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Mô phỏng thanh toán thành công"));
    }
}
