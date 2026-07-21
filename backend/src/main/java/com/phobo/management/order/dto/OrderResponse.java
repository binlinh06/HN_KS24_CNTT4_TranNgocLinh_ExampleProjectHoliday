package com.phobo.management.order.dto;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    // Detailed snapshots and attributes for Phase 6
    private String receiverNameSnapshot;
    private String receiverPhoneSnapshot;
    private String shippingAddressSnapshot;
    private String notes; // Customer notes
    private BigDecimal shippingFee;
    private String voucherCodeSnapshot;
    private List<OrderItemResponse> items;
    private Boolean canReview;
    private String reviewId;
    private LocalDateTime statusUpdatedAt;
    private String tableId;
    private String tableNumber;
    private String orderType;
}
