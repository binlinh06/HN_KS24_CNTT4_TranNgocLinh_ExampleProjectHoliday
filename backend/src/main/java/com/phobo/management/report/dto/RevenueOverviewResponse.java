package com.phobo.management.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueOverviewResponse {
    private BigDecimal grossRevenue;
    private BigDecimal discountAmount;
    private BigDecimal netRevenue;
    private Long orderCount;
    private BigDecimal averageOrderValue;
    private List<RevenueSeriesPoint> series;
    private List<TopProductReport> topProducts;
}
