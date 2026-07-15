package com.phobo.management.order.dto;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String orderCode;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String paymentAction;
    private LocalDateTime createdAt;
}
