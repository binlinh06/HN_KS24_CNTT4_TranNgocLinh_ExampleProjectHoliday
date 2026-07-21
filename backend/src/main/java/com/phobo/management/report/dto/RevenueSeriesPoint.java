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
public class RevenueSeriesPoint {
    private String period;
    private BigDecimal revenue;
    private Long orderCount;
}
