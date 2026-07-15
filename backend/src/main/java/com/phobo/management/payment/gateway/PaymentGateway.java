package com.phobo.management.payment.gateway;

import com.phobo.management.entity.OrderEntity;
import java.util.Map;

public interface PaymentGateway {
    boolean supports(String provider);
    String createPaymentUrl(OrderEntity order, String paymentId, String idempotencyKey);
    boolean verifyCallback(Map<String, String> queryParams);
}
