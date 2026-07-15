package com.phobo.management.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestCartMergeItemRequest {
    @NotNull(message = "clientItemId không được trống")
    private UUID clientItemId;

    @NotNull(message = "productId không được trống")
    private UUID productId;

    private List<UUID> optionIds;

    @NotNull(message = "quantity không được trống")
    @Min(value = 1, message = "Số lượng phải từ 1 đến 99")
    @Max(value = 99, message = "Số lượng phải từ 1 đến 99")
    private Integer quantity;

    @Size(max = 150, message = "Ghi chú không được vượt quá 150 ký tự")
    private String specialNote;
}
