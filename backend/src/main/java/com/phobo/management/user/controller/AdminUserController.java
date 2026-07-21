package com.phobo.management.user.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.user.dto.UserManagementResponse;
import com.phobo.management.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int validatedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserManagementResponse> result = adminUserService.getUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách người dùng hệ thống thành công"));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        adminUserService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái người dùng thành công"));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        adminUserService.resetUserPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success(null, "Đặt lại mật khẩu người dùng thành công"));
    }
}
