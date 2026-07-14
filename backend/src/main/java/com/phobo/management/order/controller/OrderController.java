package com.phobo.management.order.controller;

import com.phobo.management.order.service.OrderService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Order.
 * Relevant Use Cases: UC-09, UC-11, UC-12, UC-14, UC-15
 */
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-09, UC-11, UC-12, UC-14, UC-15 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(orderService.getInfo(), "Skeleton active"));
    }
}
