package com.phobo.management.attendance.service;

import com.phobo.management.attendance.dto.AttendanceAdjustRequest;
import com.phobo.management.attendance.dto.AttendanceResponse;
import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.exception.AppException;
import com.phobo.management.entity.Attendance;
import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.entity.User;
import com.phobo.management.repository.AttendanceRepository;
import com.phobo.management.repository.EmployeeProfileRepository;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.security.EmployeeAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeProfileRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeAuthorizationService employeeAuthService;
    private final AuditLogService auditLogService;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Transactional
    public AttendanceResponse checkIn() {
        String currentUserId = employeeAuthService.getCurrentUserId();
        EmployeeProfile employee = employeeRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin nhân viên", "EMPLOYEE_NOT_FOUND"));

        if (Boolean.FALSE.equals(employee.getIsActive())) {
            throw new AppException("Tài khoản nhân viên đã bị vô hiệu hóa", "EMPLOYEE_INACTIVE");
        }

        // Lock employee active attendance with PESSIMISTIC_WRITE
        Optional<Attendance> activeOpt = attendanceRepository.findActiveAttendanceForUpdate(employee.getId());
        if (activeOpt.isPresent()) {
            throw new AppException("Nhân viên đang có ca chưa check-out. Không thể check-in hai lần.", "ATTENDANCE_ALREADY_OPEN");
        }

        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        Attendance attendance = Attendance.builder()
                .id(UUID.randomUUID().toString())
                .employee(employee)
                .workDate(now.toLocalDate())
                .checkIn(now)
                .status("PRESENT")
                .checkInSource("SYSTEM")
                .build();

        attendanceRepository.save(attendance);

        auditLogService.logAction(currentUserId, "ROLE_STAFF", "CHECK_IN", "Attendance",
                attendance.getId(), "SUCCESS", "Nhân viên check-in lúc " + now, null, null);

        return mapToResponse(attendance);
    }

    @Transactional
    public AttendanceResponse checkOut() {
        String currentUserId = employeeAuthService.getCurrentUserId();
        EmployeeProfile employee = employeeRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin nhân viên", "EMPLOYEE_NOT_FOUND"));

        // Lock employee active attendance with PESSIMISTIC_WRITE
        Attendance attendance = attendanceRepository.findActiveAttendanceForUpdate(employee.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy ca chấm công đang mở để check-out", "ATTENDANCE_NOT_OPEN"));

        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        if (now.isBefore(attendance.getCheckIn())) {
            throw new AppException("Thời gian check-out không được trước thời gian check-in", "INVALID_CHECKOUT_TIME");
        }

        attendance.setCheckOut(now);
        attendance.setCheckOutSource("SYSTEM");
        attendanceRepository.save(attendance);

        auditLogService.logAction(currentUserId, "ROLE_STAFF", "CHECK_OUT", "Attendance",
                attendance.getId(), "SUCCESS", "Nhân viên check-out lúc " + now, null, null);

        return mapToResponse(attendance);
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getMyActiveAttendance() {
        String currentUserId = employeeAuthService.getCurrentUserId();
        EmployeeProfile employee = employeeRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin nhân viên", "EMPLOYEE_NOT_FOUND"));

        Attendance attendance = attendanceRepository.findByEmployeeIdAndCheckOutIsNull(employee.getId())
                .orElse(null);

        return attendance != null ? mapToResponse(attendance) : null;
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceRecords(String employeeId, LocalDate startDate, LocalDate endDate, String status, Pageable pageable) {
        return attendanceRepository.findAttendanceWithFilters(employeeId, startDate, endDate, status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public AttendanceResponse adjustAttendance(String id, AttendanceAdjustRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        User manager = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng hiện tại", "USER_NOT_FOUND"));

        if (request.getAdjustReason() == null || request.getAdjustReason().trim().isEmpty()) {
            throw new AppException("Lý do điều chỉnh chấm công không được để trống", "ATTENDANCE_ADJUSTMENT_REASON_REQUIRED");
        }

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy bản ghi chấm công", "ATTENDANCE_NOT_FOUND"));

        if (request.getCheckOut() != null && request.getCheckOut().isBefore(request.getCheckIn())) {
            throw new AppException("Thời gian check-out không được trước thời gian check-in", "INVALID_CHECKOUT_TIME");
        }

        attendance.setCheckIn(request.getCheckIn());
        attendance.setCheckOut(request.getCheckOut());
        attendance.setAdjustReason(request.getAdjustReason().trim());
        attendance.setApprovedBy(manager);
        attendance.setApprovedAt(LocalDateTime.now(VN_ZONE));
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus().trim().toUpperCase());
        } else {
            attendance.setStatus("MANUAL_ADJUSTED");
        }

        attendanceRepository.save(attendance);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "ADJUST_ATTENDANCE", "Attendance",
                attendance.getId(), "SUCCESS", "Điều chỉnh chấm công cho " + attendance.getEmployee().getFullName() + " - Lý do: " + request.getAdjustReason(), null, null);

        return mapToResponse(attendance);
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        EmployeeProfile ep = a.getEmployee();
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(ep.getId())
                .employeeName(ep.getFullName())
                .employeeCode(ep.getEmployeeCode())
                .shiftAssignmentId(a.getShiftAssignment() != null ? a.getShiftAssignment().getId() : null)
                .workDate(a.getWorkDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())
                .status(a.getStatus())
                .checkInSource(a.getCheckInSource())
                .checkOutSource(a.getCheckOutSource())
                .adjustReason(a.getAdjustReason())
                .approvedByUsername(a.getApprovedBy() != null ? a.getApprovedBy().getUsername() : null)
                .approvedAt(a.getApprovedAt())
                .build();
    }
}
