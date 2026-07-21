package com.phobo.management.shift.service;

import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.exception.AppException;
import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.entity.ShiftAssignment;
import com.phobo.management.entity.User;
import com.phobo.management.entity.WorkShift;
import com.phobo.management.repository.EmployeeProfileRepository;
import com.phobo.management.repository.ShiftAssignmentRepository;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.repository.WorkShiftRepository;
import com.phobo.management.security.EmployeeAuthorizationService;
import com.phobo.management.shift.dto.ShiftAssignmentRequest;
import com.phobo.management.shift.dto.ShiftAssignmentResponse;
import com.phobo.management.shift.dto.WorkShiftRequest;
import com.phobo.management.shift.dto.WorkShiftResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeProfileRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeAuthorizationService employeeAuthService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<WorkShiftResponse> getAllWorkShifts() {
        return workShiftRepository.findAll().stream().map(this::mapToWorkShiftResponse).collect(Collectors.toList());
    }

    @Transactional
    public WorkShiftResponse createWorkShift(WorkShiftRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        String code = request.getShiftCode().trim().toUpperCase();

        if (workShiftRepository.existsByShiftCode(code)) {
            throw new AppException("Mã ca làm đã tồn tại", "SHIFT_CODE_EXISTS");
        }

        WorkShift shift = WorkShift.builder()
                .id(UUID.randomUUID().toString())
                .shiftCode(code)
                .shiftName(request.getShiftName().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .crossesMidnight(Boolean.TRUE.equals(request.getCrossesMidnight()))
                .isActive(true)
                .build();

        workShiftRepository.save(shift);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "CREATE_WORK_SHIFT", "WorkShift",
                shift.getId(), "SUCCESS", "Tạo mẫu ca làm: " + shift.getShiftCode(), null, null);

        return mapToWorkShiftResponse(shift);
    }

    @Transactional(readOnly = true)
    public Page<ShiftAssignmentResponse> getAssignments(String employeeId, LocalDate startDate, LocalDate endDate, String status, Pageable pageable) {
        return shiftAssignmentRepository.findAssignmentsWithFilters(employeeId, startDate, endDate, status, pageable)
                .map(this::mapToAssignmentResponse);
    }

    @Transactional
    public ShiftAssignmentResponse assignShift(ShiftAssignmentRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        User manager = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng hiện tại", "USER_NOT_FOUND"));

        // Lock Employee profile with PESSIMISTIC_WRITE to prevent concurrent shift assignment races
        EmployeeProfile employee = employeeRepository.findByIdForUpdate(request.getEmployeeId())
                .orElseThrow(() -> new AppException("Không tìm thấy nhân viên", "EMPLOYEE_NOT_FOUND"));

        if (Boolean.FALSE.equals(employee.getIsActive())) {
            throw new AppException("Không thể phân ca cho nhân viên đã bị vô hiệu hóa", "EMPLOYEE_INACTIVE");
        }

        WorkShift workShift = workShiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new AppException("Không tìm thấy mẫu ca làm", "SHIFT_NOT_FOUND"));

        if (Boolean.FALSE.equals(workShift.getIsActive())) {
            throw new AppException("Mẫu ca làm đã bị ngưng hoạt động", "SHIFT_INACTIVE");
        }

        // Shift interval overlap detection
        LocalDateTime newStart = request.getWorkDate().atTime(workShift.getStartTime());
        LocalDateTime newEnd = Boolean.TRUE.equals(workShift.getCrossesMidnight())
                ? request.getWorkDate().plusDays(1).atTime(workShift.getEndTime())
                : request.getWorkDate().atTime(workShift.getEndTime());

        List<ShiftAssignment> existingAssignments = shiftAssignmentRepository
                .findByEmployeeIdAndWorkDateBetween(employee.getId(), request.getWorkDate().minusDays(1), request.getWorkDate().plusDays(1));

        for (ShiftAssignment existing : existingAssignments) {
            if ("CANCELED".equalsIgnoreCase(existing.getStatus())) continue;

            WorkShift exShift = existing.getWorkShift();
            LocalDateTime exStart = existing.getWorkDate().atTime(exShift.getStartTime());
            LocalDateTime exEnd = Boolean.TRUE.equals(exShift.getCrossesMidnight())
                    ? existing.getWorkDate().plusDays(1).atTime(exShift.getEndTime())
                    : existing.getWorkDate().atTime(exShift.getEndTime());

            if (newStart.isBefore(exEnd) && exStart.isBefore(newEnd)) {
                throw new AppException("Nhân viên đã có ca làm trùng khoảng thời gian", "SHIFT_OVERLAP");
            }
        }

        ShiftAssignment assignment = ShiftAssignment.builder()
                .id(UUID.randomUUID().toString())
                .workShift(workShift)
                .employee(employee)
                .workDate(request.getWorkDate())
                .assignedBy(manager)
                .note(request.getNote())
                .status("SCHEDULED")
                .build();

        shiftAssignmentRepository.save(assignment);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "ASSIGN_SHIFT", "ShiftAssignment",
                assignment.getId(), "SUCCESS", "Phân ca cho " + employee.getFullName() + " vào ngày " + request.getWorkDate(), null, null);

        return mapToAssignmentResponse(assignment);
    }

    @Transactional
    public void cancelAssignment(String id) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        ShiftAssignment assignment = shiftAssignmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy lịch ca làm", "SHIFT_ASSIGNMENT_NOT_FOUND"));

        assignment.setStatus("CANCELED");
        shiftAssignmentRepository.save(assignment);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "CANCEL_SHIFT_ASSIGNMENT", "ShiftAssignment",
                assignment.getId(), "SUCCESS", "Hủy ca làm của " + assignment.getEmployee().getFullName(), null, null);
    }

    private WorkShiftResponse mapToWorkShiftResponse(WorkShift ws) {
        return WorkShiftResponse.builder()
                .id(ws.getId())
                .shiftCode(ws.getShiftCode())
                .shiftName(ws.getShiftName())
                .startTime(ws.getStartTime())
                .endTime(ws.getEndTime())
                .crossesMidnight(ws.getCrossesMidnight())
                .isActive(ws.getIsActive())
                .build();
    }

    private ShiftAssignmentResponse mapToAssignmentResponse(ShiftAssignment sa) {
        WorkShift ws = sa.getWorkShift();
        EmployeeProfile ep = sa.getEmployee();
        return ShiftAssignmentResponse.builder()
                .id(sa.getId())
                .shiftId(ws.getId())
                .shiftName(ws.getShiftName())
                .startTime(ws.getStartTime())
                .endTime(ws.getEndTime())
                .employeeId(ep.getId())
                .employeeName(ep.getFullName())
                .employeeCode(ep.getEmployeeCode())
                .workDate(sa.getWorkDate())
                .note(sa.getNote())
                .status(sa.getStatus())
                .build();
    }
}
