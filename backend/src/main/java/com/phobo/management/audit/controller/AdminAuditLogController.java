package com.phobo.management.audit.controller;

import com.phobo.management.audit.dto.AuditLogResponse;
import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String actorUserId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        int validatedSize = Math.min(Math.max(size, 1), 100); // Page size limit max 100
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> result = auditLogService.getAuditLogs(actorUserId, action, entityType, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách nhật ký kiểm toán thành công"));
    }
}
