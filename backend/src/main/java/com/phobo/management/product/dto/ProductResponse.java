package com.phobo.management.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String productName;
    private String slug;
    private BigDecimal basePrice;
    private String description;
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isFeatured;
    private Integer preparationTimeMinutes;
    private CategorySummary category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String id;
        private String categoryName;
    }
}
