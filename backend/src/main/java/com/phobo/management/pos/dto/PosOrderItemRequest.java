package com.phobo.management.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosOrderItemRequest {
    private String productId;
    private Integer quantity;
    private List<String> optionIds;
}
