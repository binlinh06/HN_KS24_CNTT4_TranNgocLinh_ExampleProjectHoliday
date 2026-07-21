package com.phobo.management.table.service;

import com.phobo.management.common.enums.RestaurantTableStatus;
import com.phobo.management.common.enums.TableSessionStatus;
import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.entity.RestaurantTable;
import com.phobo.management.entity.TableSession;
import com.phobo.management.exception.OrderException;
import com.phobo.management.repository.EmployeeProfileRepository;
import com.phobo.management.repository.RestaurantTableRepository;
import com.phobo.management.repository.TableSessionRepository;
import com.phobo.management.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    private EmployeeProfile getCurrentEmployeeProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new OrderException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new OrderException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return employeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OrderException("Không tìm thấy hồ sơ nhân viên", "EMPLOYEE_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    @Transactional
    public TableSession openTable(String tableId) {
        RestaurantTable table = tableRepository.findByIdWithLock(tableId)
                .orElseThrow(() -> new OrderException("Không tìm thấy bàn ăn", "TABLE_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (table.getStatus() != RestaurantTableStatus.AVAILABLE) {
            throw new OrderException("Bàn ăn đang không sẵn sàng để mở", "TABLE_NOT_AVAILABLE", HttpStatus.BAD_REQUEST);
        }

        // Enforce active session check
        tableSessionRepository.findActiveSessionByTableId(tableId).ifPresent(s -> {
            throw new OrderException("Bàn ăn đã có phiên hoạt động", "ACTIVE_SESSION_EXISTS", HttpStatus.BAD_REQUEST);
        });

        EmployeeProfile employee = getCurrentEmployeeProfile();

        // Update table
        table.setStatus(RestaurantTableStatus.OCCUPIED);
        table.setStatusUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);

        // Create table session
        TableSession session = TableSession.builder()
                .id(UUID.randomUUID().toString())
                .table(table)
                .status(TableSessionStatus.OPEN)
                .openedBy(employee)
                .openedAt(LocalDateTime.now())
                .build();
        TableSession savedSession = tableSessionRepository.save(session);
        log.info("Opened table session {} for table {} by employee {}", savedSession.getId(), table.getId(), employee.getFullName());
        return savedSession;
    }

    @Transactional
    public void markCleaning(String tableId) {
        RestaurantTable table = tableRepository.findByIdWithLock(tableId)
                .orElseThrow(() -> new OrderException("Không tìm thấy bàn ăn", "TABLE_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (table.getStatus() != RestaurantTableStatus.OCCUPIED) {
            if (table.getStatus() == RestaurantTableStatus.CLEANING) {
                return; // Idempotency
            }
            throw new OrderException("Chỉ được dọn dẹp bàn ăn đang bận", "INVALID_TABLE_STATUS", HttpStatus.BAD_REQUEST);
        }

        // Verify active session has been closed/canceled
        tableSessionRepository.findActiveSessionByTableId(tableId).ifPresent(s -> {
            throw new OrderException("Không thể dọn dẹp khi phiên của bàn ăn chưa đóng", "ACTIVE_SESSION_EXISTS", HttpStatus.BAD_REQUEST);
        });

        table.setStatus(RestaurantTableStatus.CLEANING);
        table.setStatusUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);
        log.info("Updated table {} to CLEANING", table.getId());
    }

    @Transactional
    public void markAvailable(String tableId) {
        RestaurantTable table = tableRepository.findByIdWithLock(tableId)
                .orElseThrow(() -> new OrderException("Không tìm thấy bàn ăn", "TABLE_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (table.getStatus() != RestaurantTableStatus.CLEANING) {
            if (table.getStatus() == RestaurantTableStatus.AVAILABLE) {
                return; // Idempotency
            }
            throw new OrderException("Chỉ có thể chuyển sang sẵn sàng từ trạng thái đang dọn dẹp", "INVALID_TABLE_STATUS", HttpStatus.BAD_REQUEST);
        }

        table.setStatus(RestaurantTableStatus.AVAILABLE);
        table.setStatusUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);
        log.info("Updated table {} to AVAILABLE", table.getId());
    }

    @Transactional
    public void outOfService(String tableId) {
        RestaurantTable table = tableRepository.findByIdWithLock(tableId)
                .orElseThrow(() -> new OrderException("Không tìm thấy bàn ăn", "TABLE_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (table.getStatus() == RestaurantTableStatus.INACTIVE) {
            return; // Idempotency
        }

        // Ensure no active session exists
        tableSessionRepository.findActiveSessionByTableId(tableId).ifPresent(s -> {
            throw new OrderException("Không thể đưa bàn ra phục vụ khi đang có khách ngồi", "ACTIVE_SESSION_EXISTS", HttpStatus.BAD_REQUEST);
        });

        table.setStatus(RestaurantTableStatus.INACTIVE);
        table.setStatusUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);
        log.info("Updated table {} status to INACTIVE (OUT_OF_SERVICE)", table.getId());
    }

    @Transactional
    public void restoreTable(String tableId) {
        RestaurantTable table = tableRepository.findByIdWithLock(tableId)
                .orElseThrow(() -> new OrderException("Không tìm thấy bàn ăn", "TABLE_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (table.getStatus() != RestaurantTableStatus.INACTIVE) {
            if (table.getStatus() == RestaurantTableStatus.AVAILABLE) {
                return; // Idempotency
            }
            throw new OrderException("Chỉ có thể khôi phục bàn đang tạm dừng phục vụ", "INVALID_TABLE_STATUS", HttpStatus.BAD_REQUEST);
        }

        table.setStatus(RestaurantTableStatus.AVAILABLE);
        table.setStatusUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);
        log.info("Restored table {} status to AVAILABLE", table.getId());
    }
}
