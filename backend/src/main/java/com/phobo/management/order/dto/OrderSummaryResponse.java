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
public class OrderSummaryResponse {
    private String orderId;
    private String orderCode;
    private LocalDateTime createdAt;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal finalAmount;
    private Integer totalQuantity;
    private String itemPreview;
    private Boolean canReview;
    private String reviewStatus; // NONE, PENDING, APPROVED, REJECTED
}
