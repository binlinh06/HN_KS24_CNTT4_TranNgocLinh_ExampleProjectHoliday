package com.phobo.management.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductReport {
    private String productId;
    private String productName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}
