package com.phobo.management.order.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.OrderType;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.order.dto.OrderResponse;
import com.phobo.management.order.service.OrderAcceptanceService;
import com.phobo.management.order.service.OrderService;
import com.phobo.management.order.service.StaffEventSubscriptionService;
import com.phobo.management.repository.OrderEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/staff/orders")
@RequiredArgsConstructor
@Slf4j
public class StaffOrderController {

    private final OrderEntityRepository orderRepository;
    private final OrderAcceptanceService orderAcceptanceService;
    private final OrderService orderService;
    private final StaffEventSubscriptionService staffEventSubscriptionService;

    @GetMapping
    @PreAuthorize("@employeeAuthService.isStaffOrManager()")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getStaffOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType type) {
        
        List<OrderEntity> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<OrderResponse> filtered = orders.stream()
                .filter(o -> status == null || o.getStatus() == status)
                .filter(o -> type == null || o.getOrderType() == type)
                .map(o -> orderService.getOrderDetails(o.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(filtered, "Lấy danh sách đơn hàng thành công"));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("@employeeAuthService.isStaffOrManager()")
    public ResponseEntity<ApiResponse<OrderResponse>> getStaffOrderDetails(@PathVariable String orderId) {
        OrderResponse response = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết đơn hàng thành công"));
    }

    @PostMapping("/{orderId}/accept")
    @PreAuthorize("@employeeAuthService.isCashier()")
    public ResponseEntity<ApiResponse<Void>> acceptOrder(@PathVariable String orderId) {
        orderAcceptanceService.acceptOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xác nhận đơn hàng thành công"));
    }

    @PostMapping("/{orderId}/reject")
    @PreAuthorize("@employeeAuthService.isCashier()")
    public ResponseEntity<ApiResponse<Void>> rejectOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> payload) {
        String reason = payload != null ? payload.get("reason") : "";
        orderAcceptanceService.rejectOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Từ chối đơn hàng thành công"));
    }

    @PostMapping("/{orderId}/serve")
    @PreAuthorize("@employeeAuthService.isWaiter()")
    public ResponseEntity<ApiResponse<Void>> serveOrder(@PathVariable String orderId) {
        orderAcceptanceService.serveOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Phục vụ đơn hàng thành công"));
    }

    @PostMapping("/{orderId}/handover")
    @PreAuthorize("@employeeAuthService.isWaiter()")
    public ResponseEntity<ApiResponse<Void>> handoverOrder(@PathVariable String orderId) {
        orderAcceptanceService.handoverOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Bàn giao đơn hàng thành công"));
    }

    @GetMapping("/events")
    @PreAuthorize("@employeeAuthService.isStaffOrManager()")
    public SseEmitter subscribeToStaffEvents() {
        log.info("Staff subscribing to staff events stream");
        return staffEventSubscriptionService.subscribe();
    }
}
