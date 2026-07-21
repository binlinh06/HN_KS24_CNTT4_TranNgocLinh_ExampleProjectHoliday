package com.phobo.management.pos.dto;

import com.phobo.management.common.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosOrderRequest {
    private String tableId;
    private List<PosOrderItemRequest> items;
    private PaymentMethod paymentMethod;
    private BigDecimal discountAmount;
}
