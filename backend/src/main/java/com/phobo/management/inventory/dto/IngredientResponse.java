package com.phobo.management.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientResponse {
    private String id;
    private String ingredientCode;
    private String name;
    private String unit;
    private BigDecimal minThreshold;
    private BigDecimal currentStock;
    private Boolean isLowStock;
    private Boolean isActive;
}
