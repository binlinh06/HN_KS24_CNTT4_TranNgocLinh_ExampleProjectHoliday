package com.phobo.management.cart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private UUID id;
    private List<CartItemResponse> items;
    private Integer itemCount;
    private Integer totalQuantity;
    private BigDecimal subtotal;
    private CartVoucherResponse voucher;
    private BigDecimal discountAmount;
    private BigDecimal estimatedTotal;
    private boolean valid;
    private List<String> validationMessages;
    private String voucherRemovalReason;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartVoucherResponse {
        private UUID id;
        private String code;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderValue;
    }
}
