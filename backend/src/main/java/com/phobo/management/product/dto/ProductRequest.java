package com.phobo.management.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Tên món ăn không được trống")
    private String productName;

    private String slug;

    @NotBlank(message = "Danh mục không được trống")
    private String categoryId;

    @NotNull(message = "Giá cơ bản không được trống")
    @DecimalMin(value = "0.00", message = "Giá cơ bản không được âm")
    private BigDecimal basePrice;

    private String description;

    private String imageUrl;

    private Boolean isAvailable;

    private Boolean isFeatured;

    @NotNull(message = "Thời gian chuẩn bị không được trống")
    @Min(value = 1, message = "Thời gian chuẩn bị phải lớn hơn hoặc bằng 1 phút")
    private Integer preparationTimeMinutes;
}
