package com.phobo.management.kitchen.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.common.enums.KitchenItemStatus;
import com.phobo.management.kitchen.dto.KitchenQueueResponse;
import com.phobo.management.kitchen.service.KitchenQueueService;
import com.phobo.management.order.service.StaffEventSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff/kitchen")
@RequiredArgsConstructor
@Slf4j
public class StaffKitchenController {

    private final KitchenQueueService kitchenQueueService;
    private final StaffEventSubscriptionService staffEventSubscriptionService;

    @GetMapping("/queue")
    @PreAuthorize("@employeeAuthService.isKitchen()")
    public ResponseEntity<ApiResponse<List<KitchenQueueResponse>>> getQueue() {
        List<KitchenQueueResponse> queue = kitchenQueueService.getQueue();
        return ResponseEntity.ok(ApiResponse.success(queue, "Lấy hàng đợi nhà bếp thành công"));
    }

    @PatchMapping("/queue/{queueId}/status")
    @PreAuthorize("@employeeAuthService.isKitchen()")
    public ResponseEntity<ApiResponse<KitchenQueueResponse>> updateItemStatus(
            @PathVariable String queueId,
            @RequestBody Map<String, Object> payload) {
        
        String statusStr = (String) payload.get("status");
        Number versionNum = (Number) payload.get("version");
        
        KitchenItemStatus status = KitchenItemStatus.valueOf(statusStr.toUpperCase());
        Long version = versionNum != null ? versionNum.longValue() : null;

        log.info("Updating kitchen item {} to status {}. Expected version: {}", queueId, status, version);
        KitchenQueueResponse response = kitchenQueueService.updateItemStatus(queueId, status, version);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái món ăn thành open/cooking/ready thành công"));
    }

    @GetMapping("/events")
    @PreAuthorize("@employeeAuthService.isKitchen()")
    public SseEmitter subscribeToKitchenEvents() {
        log.info("Kitchen subscribing to kitchen events stream");
        return staffEventSubscriptionService.subscribe();
    }
}
