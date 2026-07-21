package com.phobo.management.user.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.user.dto.CreateEmployeeRequest;
import com.phobo.management.user.dto.EmployeeResponse;
import com.phobo.management.user.dto.UpdateEmployeeRequest;
import com.phobo.management.user.service.EmployeeManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/manager/employees")
@RequiredArgsConstructor
public class ManagerEmployeeController {

    private final EmployeeManagementService employeeManagementService;

    @GetMapping
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        int validatedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "hireDate"));
        Page<EmployeeResponse> result = employeeManagementService.getEmployees(keyword, position, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách nhân viên thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable String id) {
        EmployeeResponse employee = employeeManagementService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employee, "Lấy thông tin nhân viên thành công"));
    }

    @PostMapping
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeManagementService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tạo tài khoản nhân viên thành công"));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse response = employeeManagementService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật thông tin nhân viên thành công"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Void>> deactivateEmployee(@PathVariable String id) {
        employeeManagementService.deactivateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Vô hiệu hóa tài khoản nhân viên thành công"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Void>> activateEmployee(@PathVariable String id) {
        employeeManagementService.activateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Kích hoạt lại tài khoản nhân viên thành công"));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        employeeManagementService.resetPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success(null, "Đặt lại mật khẩu nhân viên thành công"));
    }
}
