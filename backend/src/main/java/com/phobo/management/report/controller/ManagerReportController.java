package com.phobo.management.report.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.report.dto.RevenueOverviewResponse;
import com.phobo.management.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/manager/reports")
@RequiredArgsConstructor
public class ManagerReportController {

    private final ReportService reportService;

    @GetMapping("/revenue")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<RevenueOverviewResponse>> getRevenueOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "DAY") String grouping) {
        RevenueOverviewResponse response = reportService.getRevenueOverview(from, to, grouping);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy báo cáo doanh thu thành công"));
    }

    @GetMapping("/export")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<byte[]> exportRevenueCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        byte[] csvBytes = reportService.exportRevenueCsv(from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"BaoCaoDoanhThu.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}
