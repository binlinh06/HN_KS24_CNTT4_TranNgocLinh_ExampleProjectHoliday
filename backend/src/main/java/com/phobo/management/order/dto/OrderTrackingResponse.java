package com.phobo.management.order.dto;

import com.phobo.management.common.enums.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private String orderId;
    private String orderCode;
    private OrderStatus currentStatus;
    private LocalDateTime statusUpdatedAt;
    private Boolean terminal;
    private List<OrderStatusHistoryResponse> timeline;
}
