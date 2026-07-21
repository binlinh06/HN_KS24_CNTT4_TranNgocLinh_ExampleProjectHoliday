package com.phobo.management.kitchen.dto;

import com.phobo.management.common.enums.KitchenItemStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenQueueResponse {
    private String id;
    private String orderItemId;
    private String orderId;
    private String orderCode;
    private String productName;
    private Integer quantity;
    private List<String> options;
    private KitchenItemStatus itemStatus;
    private Integer priority;
    private String claimedByEmployeeId;
    private String claimedByEmployeeName;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private Long version;
}
