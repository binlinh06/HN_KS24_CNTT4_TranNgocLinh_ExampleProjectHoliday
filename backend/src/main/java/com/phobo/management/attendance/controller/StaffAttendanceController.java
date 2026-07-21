package com.phobo.management.attendance.controller;

import com.phobo.management.attendance.dto.AttendanceResponse;
import com.phobo.management.attendance.service.AttendanceService;
import com.phobo.management.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/attendance")
@RequiredArgsConstructor
public class StaffAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn() {
        AttendanceResponse response = attendanceService.checkIn();
        return ResponseEntity.ok(ApiResponse.success(response, "Check-in thành công"));
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut() {
        AttendanceResponse response = attendanceService.checkOut();
        return ResponseEntity.ok(ApiResponse.success(response, "Check-out thành công"));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getMyActiveAttendance() {
        AttendanceResponse response = attendanceService.getMyActiveAttendance();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin chấm công hiện tại thành công"));
    }
}
