package com.phobo.management.order.controller;

import com.phobo.management.checkout.dto.CheckoutPreviewRequest;
import com.phobo.management.order.dto.OrderResponse;
import com.phobo.management.order.service.OrderService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Validated @RequestBody CheckoutPreviewRequest request) {
        OrderResponse response = orderService.checkoutOrder(request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success(response, "Đặt hàng thành công"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(@PathVariable String orderId) {
        OrderResponse response = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết đơn hàng thành công"));
    }
}
