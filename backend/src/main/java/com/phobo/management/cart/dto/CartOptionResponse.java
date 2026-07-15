package com.phobo.management.cart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartOptionResponse {
    private UUID id;
    private String groupName;
    private String optionName;
    private BigDecimal incrementalPrice;
}
