package com.phobo.management.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được trống")
    private String categoryName;

    private String description;

    @NotNull(message = "Thứ tự hiển thị không được trống")
    @Min(value = 0, message = "Thứ tự hiển thị phải lớn hơn hoặc bằng 0")
    private Integer displayOrder;

    private Boolean isActive;
}
