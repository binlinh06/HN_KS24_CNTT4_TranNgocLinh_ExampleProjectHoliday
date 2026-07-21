package com.phobo.management.systemconfig.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.systemconfig.dto.SystemConfigResponse;
import com.phobo.management.systemconfig.dto.UpdateSystemConfigRequest;
import com.phobo.management.systemconfig.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getAllConfigs() {
        List<SystemConfigResponse> list = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách cấu hình hệ thống thành công"));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        SystemConfigResponse response = systemConfigService.updateConfig(key, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật cấu hình hệ thống thành công"));
    }
}
