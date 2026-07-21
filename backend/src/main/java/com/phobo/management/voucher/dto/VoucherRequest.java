package com.phobo.management.voucher.dto;

import jakarta.validation.constraints.DecimalMin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // PERCENTAGE, FIXED_AMOUNT

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @NotNull(message = "Đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0.00", message = "Đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @NotNull(message = "Giới hạn sử dụng không được để trống")
    @Min(value = 1, message = "Giới hạn sử dụng phải ít nhất là 1")
    private Integer usageLimit;
}
