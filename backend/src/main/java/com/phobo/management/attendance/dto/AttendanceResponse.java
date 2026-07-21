package com.phobo.management.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private String id;
    private String employeeId;
    private String employeeName;
    private String employeeCode;
    private String shiftAssignmentId;
    private LocalDate workDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private String checkInSource;
    private String checkOutSource;
    private String adjustReason;
    private String approvedByUsername;
    private LocalDateTime approvedAt;
}
