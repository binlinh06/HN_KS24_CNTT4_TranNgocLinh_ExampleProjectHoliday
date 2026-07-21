package com.phobo.management.table.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.entity.RestaurantTable;
import com.phobo.management.entity.TableSession;
import com.phobo.management.repository.RestaurantTableRepository;
import com.phobo.management.table.service.TableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/tables")
@RequiredArgsConstructor
@Slf4j
public class StaffTableController {

    private final RestaurantTableRepository tableRepository;
    private final TableService tableService;

    @GetMapping
    @PreAuthorize("@employeeAuthService.isStaffOrManager()")
    public ResponseEntity<ApiResponse<List<RestaurantTable>>> getTables() {
        List<RestaurantTable> tables = tableRepository.findAll(Sort.by(Sort.Direction.ASC, "tableNumber"));
        return ResponseEntity.ok(ApiResponse.success(tables, "Lấy danh sách bàn ăn thành công"));
    }

    @PostMapping("/{tableId}/open")
    @PreAuthorize("@employeeAuthService.isWaiter()")
    public ResponseEntity<ApiResponse<TableSession>> openTable(@PathVariable String tableId) {
        TableSession session = tableService.openTable(tableId);
        return ResponseEntity.ok(ApiResponse.success(session, "Mở bàn thành công"));
    }

    @PostMapping("/{tableId}/mark-cleaning")
    @PreAuthorize("@employeeAuthService.isWaiter()")
    public ResponseEntity<ApiResponse<Void>> markCleaning(@PathVariable String tableId) {
        tableService.markCleaning(tableId);
        return ResponseEntity.ok(ApiResponse.success(null, "Chuyển bàn sang trạng thái dọn dẹp thành công"));
    }

    @PostMapping("/{tableId}/mark-available")
    @PreAuthorize("@employeeAuthService.isWaiter()")
    public ResponseEntity<ApiResponse<Void>> markAvailable(@PathVariable String tableId) {
        tableService.markAvailable(tableId);
        return ResponseEntity.ok(ApiResponse.success(null, "Chuyển bàn sang trạng thái sẵn sàng thành công"));
    }

    @PostMapping("/{tableId}/out-of-service")
    @PreAuthorize("@employeeAuthService.isWaiter()")
    public ResponseEntity<ApiResponse<Void>> outOfService(@PathVariable String tableId) {
        tableService.outOfService(tableId);
        return ResponseEntity.ok(ApiResponse.success(null, "Tạm dừng phục vụ bàn ăn thành công"));
    }

    @PostMapping("/{tableId}/restore")
    @PreAuthorize("@employeeAuthService.isWaiter()")
    public ResponseEntity<ApiResponse<Void>> restoreTable(@PathVariable String tableId) {
        tableService.restoreTable(tableId);
        return ResponseEntity.ok(ApiResponse.success(null, "Khôi phục bàn ăn thành công"));
    }
}
