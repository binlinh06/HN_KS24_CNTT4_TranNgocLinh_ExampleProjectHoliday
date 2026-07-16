package com.phobo.management.order.controller;

import com.phobo.management.checkout.dto.CheckoutPreviewRequest;
import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.order.dto.OrderResponse;
import com.phobo.management.order.dto.OrderSummaryResponse;
import com.phobo.management.order.dto.OrderTrackingResponse;
import com.phobo.management.order.service.OrderService;
import com.phobo.management.order.service.OrderTrackingSubscriptionService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;
    private final OrderTrackingSubscriptionService subscriptionService;

    public OrderController(
            OrderService orderService,
            OrderTrackingSubscriptionService subscriptionService) {
        this.orderService = orderService;
        this.subscriptionService = subscriptionService;
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

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> getOrderTracking(@PathVariable String orderId) {
        OrderTrackingResponse response = orderService.getOrderTracking(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin theo dõi đơn hàng thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<OrderSummaryResponse>>> getOrdersHistory(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        org.springframework.data.domain.Page<OrderSummaryResponse> response = orderService.getOrdersHistory(
                status, orderCode, from, to, page, size, sortField, sortDirection
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy lịch sử đơn hàng thành công"));
    }

    @GetMapping("/{orderId}/events")
    public SseEmitter subscribeOrderStatus(@PathVariable String orderId) {
        // 1. Verify ownership first (throws exception if not owner/not found)
        OrderTrackingResponse snapshot = orderService.getOrderTracking(orderId);

        // 2. Subscribe
        SseEmitter emitter = subscriptionService.subscribe(orderId);

        // 3. Send initial snapshot immediately in the same connection
        try {
            emitter.send(SseEmitter.event()
                    .id("INIT-" + orderId)
                    .name("INIT")
                    .data(snapshot)
                    .build());
        } catch (IOException | IllegalStateException e) {
            // Emitter will be handled/cleaned up by subscriptionService
        }

        return emitter;
    }
}
