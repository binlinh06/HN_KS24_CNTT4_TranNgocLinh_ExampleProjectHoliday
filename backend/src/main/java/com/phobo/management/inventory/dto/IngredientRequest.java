package com.phobo.management.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class IngredientRequest {
    @NotBlank(message = "Mã nguyên liệu không được để trống")
    private String ingredientCode;

    @NotBlank(message = "Tên nguyên liệu không được để trống")
    private String name;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;

    @NotNull(message = "Ngưỡng tối thiểu không được để trống")
    @DecimalMin(value = "0.0000", message = "Ngưỡng tối thiểu không được âm")
    private BigDecimal minThreshold;

    @DecimalMin(value = "0.0000", message = "Tồn kho ban đầu không được âm")
    private BigDecimal initialStock;
}
