package com.phobo.management.user.service;

import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.common.enums.RoleCode;
import com.phobo.management.common.enums.UserStatus;
import com.phobo.management.entity.User;
import com.phobo.management.exception.AppException;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.repository.UserRoleRepository;
import com.phobo.management.security.EmployeeAuthorizationService;
import com.phobo.management.user.dto.UserManagementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeAuthorizationService employeeAuthService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<UserManagementResponse> getUsers(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    keyword.trim(), keyword.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::mapToResponse);
    }

    @Transactional
    public void updateUserStatus(String userId, String newStatusStr) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new AppException("Không thể tự thay đổi trạng thái tài khoản của chính mình", "SELF_LOCKOUT_PROTECTION");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(newStatusStr.trim().toUpperCase());
        } catch (Exception e) {
            throw new AppException("Trạng thái không hợp lệ", "INVALID_STATUS");
        }

        List<String> roles = getUserRoles(userId);
        if (roles.contains("ADMIN")) {
            long activeAdminCount = countActiveAdmins();
            if (activeAdminCount <= 1 && newStatus != UserStatus.ACTIVE) {
                throw new AppException("Không thể khóa hoặc vô hiệu hóa tài khoản Quản trị viên (Admin) duy nhất còn lại", "LAST_ADMIN_PROTECTION");
            }
        }

        UserStatus oldStatus = targetUser.getStatus();
        targetUser.setStatus(newStatus);
        userRepository.save(targetUser);

        auditLogService.logAction(currentUserId, "ROLE_ADMIN", "UPDATE_USER_STATUS", "User",
                targetUser.getId(), "SUCCESS", "Đổi trạng thái người dùng " + targetUser.getUsername() + " từ " + oldStatus + " sang " + newStatus, null, null);
    }

    @Transactional
    public void resetUserPassword(String userId, String newPassword) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        if (newPassword == null || newPassword.length() < 6) {
            throw new AppException("Mật khẩu mới phải có ít nhất 6 ký tự", "INVALID_PASSWORD");
        }

        targetUser.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(targetUser);

        auditLogService.logAction(currentUserId, "ROLE_ADMIN", "RESET_USER_PASSWORD", "User",
                targetUser.getId(), "SUCCESS", "Đặt lại mật khẩu cho người dùng: " + targetUser.getUsername(), null, null);
    }

    private long countActiveAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE && u.getDeletedAt() == null)
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.getCode() == RoleCode.ADMIN))
                .count();
    }

    private List<String> getUserRoles(String userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRoles() != null ? u.getRoles().stream().map(r -> r.getCode().name()).collect(Collectors.toList()) : List.<String>of())
                .orElse(List.of());
    }

    private UserManagementResponse mapToResponse(User user) {
        List<String> roles = getUserRoles(user.getId());
        return UserManagementResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus() != null ? user.getStatus().name() : "INACTIVE")
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
