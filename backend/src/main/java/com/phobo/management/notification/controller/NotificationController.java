package com.phobo.management.notification.controller;

import com.phobo.management.notification.service.NotificationService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Notification.
 * Utility Module
 */
@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for  in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(notificationService.getInfo(), "Skeleton active"));
    }
}
