package com.phobo.management.shift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignmentResponse {
    private String id;
    private String shiftId;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate workDate;
    private String note;
    private String status;
}
