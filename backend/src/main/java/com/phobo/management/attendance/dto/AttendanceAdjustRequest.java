package com.phobo.management.attendance.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceAdjustRequest {
    @NotNull(message = "Thời gian check-in không được để trống")
    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    @NotBlank(message = "Lý do điều chỉnh không được để trống")
    private String adjustReason;

    private String status; // PRESENT, LATE, EARLY_LEAVE, ABSENT, MANUAL_ADJUSTED
}
