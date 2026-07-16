package com.phobo.management.order.dto;

import com.phobo.management.common.enums.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryResponse {
    private OrderStatus previousStatus;
    private OrderStatus status;
    private LocalDateTime changedAt;
    private String changedByRole;
    private String source;
    private String reason;
}
