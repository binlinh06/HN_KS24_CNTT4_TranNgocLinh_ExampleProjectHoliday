package com.phobo.management.invoice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private String id;
    private String invoiceNumber;
    private String orderId;
    private String orderCode;
    private String orderType;
    private String tableNumber;
    private String issuedByName;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private LocalDateTime issuedAt;
    private Integer printCount;
    private LocalDateTime lastPrintedAt;
    private List<InvoiceItemResponse> items;
}
