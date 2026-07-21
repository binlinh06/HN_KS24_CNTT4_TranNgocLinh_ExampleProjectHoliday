package com.phobo.management.shift.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.shift.dto.ShiftAssignmentRequest;
import com.phobo.management.shift.dto.ShiftAssignmentResponse;
import com.phobo.management.shift.dto.WorkShiftRequest;
import com.phobo.management.shift.dto.WorkShiftResponse;
import com.phobo.management.shift.service.ShiftService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/shifts")
@RequiredArgsConstructor
public class ManagerShiftController {

    private final ShiftService shiftService;

    @GetMapping("/templates")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<List<WorkShiftResponse>>> getAllWorkShifts() {
        List<WorkShiftResponse> list = shiftService.getAllWorkShifts();
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách ca làm mẫu thành công"));
    }

    @PostMapping("/templates")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<WorkShiftResponse>> createWorkShift(@Valid @RequestBody WorkShiftRequest request) {
        WorkShiftResponse response = shiftService.createWorkShift(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tạo mẫu ca làm thành công"));
    }

    @GetMapping("/assignments")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Page<ShiftAssignmentResponse>>> getAssignments(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        int validatedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "workDate"));
        Page<ShiftAssignmentResponse> result = shiftService.getAssignments(employeeId, startDate, endDate, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy lịch phân công ca làm thành công"));
    }

    @PostMapping("/assignments")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<ShiftAssignmentResponse>> assignShift(@Valid @RequestBody ShiftAssignmentRequest request) {
        ShiftAssignmentResponse response = shiftService.assignShift(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Phân công ca làm thành công"));
    }

    @PostMapping("/assignments/{id}/cancel")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Void>> cancelAssignment(@PathVariable String id) {
        shiftService.cancelAssignment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Hủy lịch ca làm thành công"));
    }
}
