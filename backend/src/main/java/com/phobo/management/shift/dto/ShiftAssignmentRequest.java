package com.phobo.management.shift.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ShiftAssignmentRequest {
    @NotBlank(message = "ID ca làm mẫu không được để trống")
    private String shiftId;

    @NotBlank(message = "ID nhân viên không được để trống")
    private String employeeId;

    @NotNull(message = "Ngày làm việc không được để trống")
    private LocalDate workDate;

    private String note;
}
