package com.phobo.management.shift.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class WorkShiftRequest {
    @NotBlank(message = "Mã ca không được để trống")
    private String shiftCode;

    @NotBlank(message = "Tên ca không được để trống")
    private String shiftName;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    private Boolean crossesMidnight = false;
}
