package com.phobo.management.inventory.dto;

import jakarta.validation.constraints.DecimalMin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InventoryStockAdjustmentRequest {
    @NotBlank(message = "ID nguyên liệu không được để trống")
    private String ingredientId;

    @NotBlank(message = "Loại giao dịch không được để trống")
    private String transactionType; // IMPORT, EXPORT, ADJUST_IN, ADJUST_OUT, WASTE

    @NotNull(message = "Số lượng không được để trống")
    @DecimalMin(value = "0.0001", message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantity;

    private String note;
}
