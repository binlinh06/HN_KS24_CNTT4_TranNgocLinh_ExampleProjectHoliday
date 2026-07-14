package com.phobo.management.payment.controller;

import com.phobo.management.payment.service.PaymentService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Payment.
 * Relevant Use Cases: UC-10, UC-18
 */
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-10, UC-18 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(paymentService.getInfo(), "Skeleton active"));
    }
}
