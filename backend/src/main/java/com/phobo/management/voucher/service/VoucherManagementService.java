package com.phobo.management.voucher.service;

import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.exception.AppException;
import com.phobo.management.entity.Voucher;
import com.phobo.management.repository.VoucherRepository;
import com.phobo.management.security.EmployeeAuthorizationService;
import com.phobo.management.voucher.dto.VoucherRequest;
import com.phobo.management.voucher.dto.VoucherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherManagementService {

    private final VoucherRepository voucherRepository;
    private final EmployeeAuthorizationService employeeAuthService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<VoucherResponse> getVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(String id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy mã giảm giá", "VOUCHER_NOT_FOUND"));
        return mapToResponse(voucher);
    }

    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        String normalizedCode = request.getCode().trim().toUpperCase();

        if (voucherRepository.existsByCode(normalizedCode)) {
            throw new AppException("Mã voucher đã tồn tại", "VOUCHER_CODE_EXISTS");
        }
        if (request.getStartDate().isAfter(request.getEndDate()) || request.getStartDate().isEqual(request.getEndDate())) {
            throw new AppException("Thời gian bắt đầu phải trước thời gian kết thúc", "INVALID_VOUCHER_PERIOD");
        }
        if ("PERCENTAGE".equalsIgnoreCase(request.getDiscountType()) && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new AppException("Giảm giá theo phần trăm không được vượt quá 100%", "INVALID_VOUCHER_VALUE");
        }

        Voucher voucher = Voucher.builder()
                .id(UUID.randomUUID().toString())
                .code(normalizedCode)
                .discountType(request.getDiscountType().toUpperCase())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .isActive(true)
                .build();

        voucherRepository.save(voucher);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "CREATE_VOUCHER", "Voucher",
                voucher.getId(), "SUCCESS", "Tạo khuyến mãi mới: " + voucher.getCode(), null, null);

        return mapToResponse(voucher);
    }

    @Transactional
    public VoucherResponse updateVoucher(String id, VoucherRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy mã giảm giá", "VOUCHER_NOT_FOUND"));

        String normalizedCode = request.getCode().trim().toUpperCase();
        if (!voucher.getCode().equalsIgnoreCase(normalizedCode) && voucherRepository.existsByCode(normalizedCode)) {
            throw new AppException("Mã voucher đã tồn tại", "VOUCHER_CODE_EXISTS");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException("Thời gian bắt đầu phải trước thời gian kết thúc", "INVALID_VOUCHER_PERIOD");
        }

        voucher.setCode(normalizedCode);
        voucher.setDiscountType(request.getDiscountType().toUpperCase());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setUsageLimit(request.getUsageLimit());
        // Preserve usedCount! Do NOT reset usedCount!

        voucherRepository.save(voucher);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "UPDATE_VOUCHER", "Voucher",
                voucher.getId(), "SUCCESS", "Cập nhật khuyến mãi: " + voucher.getCode(), null, null);

        return mapToResponse(voucher);
    }

    @Transactional
    public void activateVoucher(String id) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy mã giảm giá", "VOUCHER_NOT_FOUND"));

        voucher.setIsActive(true);
        voucherRepository.save(voucher);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "ACTIVATE_VOUCHER", "Voucher",
                voucher.getId(), "SUCCESS", "Kích hoạt khuyến mãi: " + voucher.getCode(), null, null);
    }

    @Transactional
    public void deactivateVoucher(String id) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy mã giảm giá", "VOUCHER_NOT_FOUND"));

        voucher.setIsActive(false);
        voucherRepository.save(voucher);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "DEACTIVATE_VOUCHER", "Voucher",
                voucher.getId(), "SUCCESS", "Tắt khuyến mãi: " + voucher.getCode(), null, null);
    }

    private VoucherResponse mapToResponse(Voucher v) {
        return VoucherResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .usageLimit(v.getUsageLimit())
                .usedCount(v.getUsedCount())
                .isActive(v.getIsActive())
                .build();
    }
}
