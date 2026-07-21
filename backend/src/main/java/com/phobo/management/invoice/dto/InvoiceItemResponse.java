package com.phobo.management.invoice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemResponse {
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;
    private List<String> options;
}
