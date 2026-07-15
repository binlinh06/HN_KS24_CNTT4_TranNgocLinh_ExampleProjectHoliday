package com.phobo.management.cart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private UUID id;
    private CartProductResponse product;
    private List<CartOptionResponse> selectedOptions;
    private Integer quantity;
    private String specialNote;
    private BigDecimal basePrice;
    private BigDecimal optionsPrice;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal currentUnitPrice;
    private BigDecimal lineTotal;
    private boolean priceChanged;
    private boolean valid;
    private List<String> validationMessages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartProductResponse {
        private UUID id;
        private String name;
        private String slug;
        private String imageUrl;
        private boolean available;
    }
}
