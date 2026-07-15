package com.phobo.management.cart.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartMergeResponse {
    private List<UUID> acceptedItems;
    private List<RejectedItem> rejectedItems;
    private CartResponse cart;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RejectedItem {
        private UUID clientItemId;
        private String errorCode;
        private String message;
    }
}
