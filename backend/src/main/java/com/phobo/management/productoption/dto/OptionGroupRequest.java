package com.phobo.management.productoption.dto;

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
public class OptionGroupRequest {
    @NotBlank(message = "Tên nhóm tùy chọn không được trống")
    private String groupName;

    private Boolean isRequired;

    @NotNull(message = "Số lượng chọn tối thiểu không được trống")
    @Min(value = 0, message = "Số lượng chọn tối thiểu phải lớn hơn hoặc bằng 0")
    private Integer minSelectable;

    @NotNull(message = "Số lượng chọn tối đa không được trống")
    @Min(value = 1, message = "Số lượng chọn tối đa phải lớn hơn hoặc bằng 1")
    private Integer maxSelectable;

    private Integer displayOrder;

    private Boolean isActive;
}
