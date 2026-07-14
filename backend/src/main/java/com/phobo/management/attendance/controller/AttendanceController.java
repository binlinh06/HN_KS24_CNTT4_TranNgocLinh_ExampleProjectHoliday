package com.phobo.management.attendance.controller;

import com.phobo.management.attendance.service.AttendanceService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Attendance.
 * Relevant Use Cases: UC-28
 */
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-28 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getInfo(), "Skeleton active"));
    }
}
