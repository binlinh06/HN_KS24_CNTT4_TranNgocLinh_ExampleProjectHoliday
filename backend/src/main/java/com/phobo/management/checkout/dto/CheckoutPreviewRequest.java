package com.phobo.management.checkout.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPreviewRequest {
    private String addressId;
    private String paymentMethod;
    private String customerNote;
}
