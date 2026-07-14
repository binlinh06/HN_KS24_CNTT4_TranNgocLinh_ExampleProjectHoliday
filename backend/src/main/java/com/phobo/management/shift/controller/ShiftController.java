package com.phobo.management.shift.controller;

import com.phobo.management.shift.service.ShiftService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Shift.
 * Relevant Use Cases: UC-28
 */
@RestController
@RequestMapping("/api/v1/shift")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-28 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(shiftService.getInfo(), "Skeleton active"));
    }
}
