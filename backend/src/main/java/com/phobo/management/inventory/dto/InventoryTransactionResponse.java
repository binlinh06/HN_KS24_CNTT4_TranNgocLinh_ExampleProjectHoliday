package com.phobo.management.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {
    private String id;
    private String ingredientId;
    private String ingredientName;
    private String ingredientCode;
    private String unit;
    private String transactionType;
    private BigDecimal quantity;
    private BigDecimal stockBefore;
    private BigDecimal stockAfter;
    private String performedByEmployeeName;
    private String note;
    private LocalDateTime createdAt;
}
