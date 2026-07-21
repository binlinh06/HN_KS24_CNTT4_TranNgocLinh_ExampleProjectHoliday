package com.phobo.management.attendance.controller;

import com.phobo.management.attendance.dto.AttendanceAdjustRequest;
import com.phobo.management.attendance.dto.AttendanceResponse;
import com.phobo.management.attendance.service.AttendanceService;
import com.phobo.management.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/manager/attendance")
@RequiredArgsConstructor
public class ManagerAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAttendanceRecords(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        int validatedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "checkIn"));
        Page<AttendanceResponse> result = attendanceService.getAttendanceRecords(employeeId, startDate, endDate, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy bảng chấm công thành công"));
    }

    @PatchMapping("/{id}/adjust")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<AttendanceResponse>> adjustAttendance(
            @PathVariable String id,
            @Valid @RequestBody AttendanceAdjustRequest request) {
        AttendanceResponse response = attendanceService.adjustAttendance(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Điều chỉnh chấm công thành công"));
    }
}
