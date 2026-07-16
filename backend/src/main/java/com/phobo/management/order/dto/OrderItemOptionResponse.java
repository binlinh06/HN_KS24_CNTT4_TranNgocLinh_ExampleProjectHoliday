package com.phobo.management.order.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOptionResponse {
    private String id;
    private String optionGroupNameSnapshot;
    private String optionNameSnapshot;
    private BigDecimal incrementalPriceSnapshot;
}
