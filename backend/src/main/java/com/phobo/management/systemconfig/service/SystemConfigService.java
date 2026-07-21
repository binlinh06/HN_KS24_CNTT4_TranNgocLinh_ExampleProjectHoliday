package com.phobo.management.systemconfig.service;

import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.exception.AppException;
import com.phobo.management.entity.SystemConfiguration;
import com.phobo.management.repository.SystemConfigurationRepository;
import com.phobo.management.security.EmployeeAuthorizationService;
import com.phobo.management.systemconfig.dto.SystemConfigResponse;
import com.phobo.management.systemconfig.dto.UpdateSystemConfigRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigurationRepository configRepository;
    private final EmployeeAuthorizationService employeeAuthService;
    private final AuditLogService auditLogService;

    // Whitelisted keys forbidden from containing sensitive secrets
    private static final Set<String> ALLOWED_KEYS = Set.of(
            "store_name", "store_phone", "store_address", "opening_time", "closing_time",
            "default_shipping_fee", "order_auto_cancel_minutes", "low_stock_threshold",
            "invoice_footer", "review_public_enabled", "vat_rate"
    );

    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getPublicConfigs() {
        return configRepository.findByIsPublicTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SystemConfigResponse updateConfig(String key, UpdateSystemConfigRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        String normalizedKey = key.trim().toLowerCase();

        if (!ALLOWED_KEYS.contains(normalizedKey)) {
            throw new AppException("Khóa cấu hình không hợp lệ hoặc không có quyền chỉnh sửa", "SETTING_NOT_EDITABLE");
        }

        SystemConfiguration config = configRepository.findByConfigKey(normalizedKey)
                .orElseThrow(() -> new AppException("Không tìm thấy cấu hình " + normalizedKey, "SETTING_NOT_FOUND"));

        validateConfigValue(config.getValueType(), request.getConfigValue());

        config.setConfigValue(request.getConfigValue().trim());
        config.setUpdatedBy(currentUserId);
        configRepository.save(config);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "UPDATE_CONFIG", "SystemConfiguration",
                config.getId(), "SUCCESS", "Cập nhật cấu hình " + normalizedKey + " = " + request.getConfigValue(), null, null);

        return mapToResponse(config);
    }

    private void validateConfigValue(String valueType, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new AppException("Giá trị cấu hình không được rỗng", "INVALID_SETTING_VALUE");
        }
        try {
            switch (valueType.toUpperCase()) {
                case "INTEGER":
                    Integer.parseInt(value.trim());
                    break;
                case "DECIMAL":
                    Double.parseDouble(value.trim());
                    break;
                case "BOOLEAN":
                    if (!value.trim().equalsIgnoreCase("true") && !value.trim().equalsIgnoreCase("false")) {
                        throw new AppException("Giá trị Boolean phải là true hoặc false", "INVALID_SETTING_VALUE");
                    }
                    break;
                case "TIME":
                    java.time.LocalTime.parse(value.trim());
                    break;
                case "STRING":
                default:
                    break;
            }
        } catch (Exception e) {
            throw new AppException("Giá trị không khớp với kiểu dữ liệu " + valueType, "INVALID_SETTING_VALUE");
        }
    }

    private SystemConfigResponse mapToResponse(SystemConfiguration sc) {
        return SystemConfigResponse.builder()
                .id(sc.getId())
                .configKey(sc.getConfigKey())
                .configValue(sc.getConfigValue())
                .configGroup(sc.getConfigGroup())
                .description(sc.getDescription())
                .valueType(sc.getValueType())
                .isPublic(sc.getIsPublic())
                .build();
    }
}
