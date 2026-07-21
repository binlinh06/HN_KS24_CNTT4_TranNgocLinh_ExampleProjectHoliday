package com.phobo.management.user.service;

import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.common.enums.RoleCode;
import com.phobo.management.common.enums.UserStatus;
import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.entity.Role;
import com.phobo.management.entity.User;
import com.phobo.management.entity.UserRole;
import com.phobo.management.exception.AppException;
import com.phobo.management.repository.EmployeeProfileRepository;
import com.phobo.management.repository.RoleRepository;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.repository.UserRoleRepository;
import com.phobo.management.security.EmployeeAuthorizationService;
import com.phobo.management.user.dto.CreateEmployeeRequest;
import com.phobo.management.user.dto.EmployeeResponse;
import com.phobo.management.user.dto.UpdateEmployeeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeManagementService {

    private final EmployeeProfileRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeAuthorizationService employeeAuthService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployees(String keyword, String position, Boolean isActive, Pageable pageable) {
        return employeeRepository.findEmployeesWithFilters(keyword, position, isActive, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(String id) {
        EmployeeProfile employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy nhân viên", "EMPLOYEE_NOT_FOUND"));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Tên đăng nhập đã tồn tại", "EMPLOYEE_CODE_EXISTS");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new AppException("Email đã tồn tại", "EMPLOYEE_EMAIL_EXISTS");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException("Số điện thoại đã tồn tại", "EMPLOYEE_PHONE_EXISTS");
        }

        String normalizedPos = request.getPosition().trim().toUpperCase();
        if (!normalizedPos.equals("CASHIER") && !normalizedPos.equals("KITCHEN") && !normalizedPos.equals("WAITER") && !normalizedPos.equals("STAFF")) {
            throw new AppException("Vị trí công việc không hợp lệ", "INVALID_EMPLOYEE_POSITION");
        }

        User user = User.builder()
                .id(java.util.UUID.randomUUID().toString())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(normalizedEmail)
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        Role staffRole = roleRepository.findByCode(RoleCode.STAFF)
                .orElseThrow(() -> new AppException("Role STAFF không tồn tại", "ROLE_NOT_FOUND"));

        UserRole userRole = UserRole.builder()
                .userId(user.getId())
                .roleId(staffRole.getId())
                .user(user)
                .role(staffRole)
                .build();
        userRoleRepository.save(userRole);

        long count = employeeRepository.count() + 1;
        String employeeCode = String.format("EMP-%04d", count);

        EmployeeProfile profile = EmployeeProfile.builder()
                .id(java.util.UUID.randomUUID().toString())
                .user(user)
                .fullName(request.getFullName())
                .position(normalizedPos)
                .employeeCode(employeeCode)
                .isActive(true)
                .hireDate(LocalDateTime.now())
                .build();
        employeeRepository.save(profile);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "CREATE_EMPLOYEE", "EmployeeProfile",
                profile.getId(), "SUCCESS", "Tạo tài khoản nhân viên mới: " + profile.getEmployeeCode(), null, null);

        return mapToResponse(profile);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String id, UpdateEmployeeRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        EmployeeProfile employee = employeeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException("Không tìm thấy nhân viên", "EMPLOYEE_NOT_FOUND"));

        String normalizedPos = request.getPosition().trim().toUpperCase();
        if (!normalizedPos.equals("CASHIER") && !normalizedPos.equals("KITCHEN") && !normalizedPos.equals("WAITER") && !normalizedPos.equals("STAFF")) {
            throw new AppException("Vị trí công việc không hợp lệ", "INVALID_EMPLOYEE_POSITION");
        }

        User user = employee.getUser();
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new AppException("Email đã tồn tại", "EMPLOYEE_EMAIL_EXISTS");
        }
        if (!user.getPhone().equals(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new AppException("Số điện thoại đã tồn tại", "EMPLOYEE_PHONE_EXISTS");
        }

        user.setEmail(normalizedEmail);
        user.setPhone(request.getPhone());
        userRepository.save(user);

        employee.setFullName(request.getFullName());
        employee.setPosition(normalizedPos);
        employeeRepository.save(employee);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "UPDATE_EMPLOYEE", "EmployeeProfile",
                employee.getId(), "SUCCESS", "Cập nhật thông tin nhân viên: " + employee.getEmployeeCode(), null, null);

        return mapToResponse(employee);
    }

    @Transactional
    public void deactivateEmployee(String id) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        EmployeeProfile employee = employeeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException("Không tìm thấy nhân viên", "EMPLOYEE_NOT_FOUND"));

        employee.setIsActive(false);
        User user = employee.getUser();
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        employeeRepository.save(employee);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "DEACTIVATE_EMPLOYEE", "EmployeeProfile",
                employee.getId(), "SUCCESS", "Vô hiệu hóa tài khoản nhân viên: " + employee.getEmployeeCode(), null, null);
    }

    @Transactional
    public void activateEmployee(String id) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        EmployeeProfile employee = employeeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException("Không tìm thấy nhân viên", "EMPLOYEE_NOT_FOUND"));

        employee.setIsActive(true);
        User user = employee.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        employeeRepository.save(employee);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "ACTIVATE_EMPLOYEE", "EmployeeProfile",
                employee.getId(), "SUCCESS", "Kích hoạt lại tài khoản nhân viên: " + employee.getEmployeeCode(), null, null);
    }

    @Transactional
    public void resetPassword(String id, String newPassword) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        EmployeeProfile employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy nhân viên", "EMPLOYEE_NOT_FOUND"));

        if (newPassword == null || newPassword.length() < 6) {
            throw new AppException("Mật khẩu mới phải có ít nhất 6 ký tự", "INVALID_PASSWORD");
        }

        User user = employee.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "RESET_PASSWORD_EMPLOYEE", "EmployeeProfile",
                employee.getId(), "SUCCESS", "Đặt lại mật khẩu cho nhân viên: " + employee.getEmployeeCode(), null, null);
    }

    private EmployeeResponse mapToResponse(EmployeeProfile ep) {
        User u = ep.getUser();
        return EmployeeResponse.builder()
                .id(ep.getId())
                .userId(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .phone(u.getPhone())
                .fullName(ep.getFullName())
                .employeeCode(ep.getEmployeeCode())
                .position(ep.getPosition())
                .isActive(ep.getIsActive())
                .hireDate(ep.getHireDate())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
