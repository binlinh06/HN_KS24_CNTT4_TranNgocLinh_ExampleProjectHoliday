package com.phobo.management.checkout.dto;

import com.phobo.management.address.dto.AddressResponse;
import com.phobo.management.cart.dto.CartItemResponse;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPreviewResponse {
    private AddressResponse address;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private String appliedVoucherCode;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private List<String> warnings;
    private boolean valid;
}
