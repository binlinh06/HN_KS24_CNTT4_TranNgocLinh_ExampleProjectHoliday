package com.phobo.management.productoption.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionRequest {
    @NotBlank(message = "Tên tùy chọn không được trống")
    private String optionName;

    @NotNull(message = "Giá cộng thêm không được trống")
    @DecimalMin(value = "0.00", message = "Giá cộng thêm không được âm")
    private BigDecimal incrementalPrice;

    @Min(value = 0, message = "Thứ tự hiển thị phải lớn hơn hoặc bằng 0")
    private Integer displayOrder;

    private Boolean isAvailable;
}
