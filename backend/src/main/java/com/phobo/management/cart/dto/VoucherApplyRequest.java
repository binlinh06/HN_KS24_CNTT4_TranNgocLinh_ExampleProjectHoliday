package com.phobo.management.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherApplyRequest {
    @NotBlank(message = "Mã giảm giá không được để trống")
    private String code;
}
