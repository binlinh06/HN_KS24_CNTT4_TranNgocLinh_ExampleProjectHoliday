package com.phobo.management.report.controller;

import com.phobo.management.report.service.ReportService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Report.
 * Relevant Use Cases: UC-27
 */
@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-27 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(reportService.getInfo(), "Skeleton active"));
    }
}
