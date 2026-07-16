package com.phobo.management.order.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String id;
    private String productNameSnapshot;
    private BigDecimal basePriceSnapshot;
    private BigDecimal optionsPriceSnapshot;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal lineTotal;
    private String specialNote;
    private Integer quantity;
    private List<OrderItemOptionResponse> options;
}
