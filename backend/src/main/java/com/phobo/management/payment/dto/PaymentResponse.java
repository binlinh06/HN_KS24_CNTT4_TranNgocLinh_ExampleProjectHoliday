package com.phobo.management.payment.dto;

import com.phobo.management.common.enums.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String orderId;
    private String paymentId;
    private String provider;
    private String paymentUrl;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String providerTransactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
