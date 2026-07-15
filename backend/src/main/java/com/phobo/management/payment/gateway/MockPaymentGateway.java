package com.phobo.management.payment.gateway;

import com.phobo.management.entity.OrderEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Profile({"dev", "test"})
public class MockPaymentGateway implements PaymentGateway {

    @Value("${payment.mock.hmac-secret:mock_secret_key}")
    private String hmacSecret;

    @Override
    public boolean supports(String provider) {
        return "MOCK".equalsIgnoreCase(provider);
    }

    @Override
    public String createPaymentUrl(OrderEntity order, String paymentId, String idempotencyKey) {
        String amountStr = order.getFinalAmount().setScale(2).toPlainString();
        String orderId = order.getId();
        
        // Compute signature for redirect url validation if needed
        String signature = calculateHmac(amountStr + "|" + orderId + "|" + paymentId + "|PENDING", hmacSecret);

        return String.format("http://localhost:3000/customer/checkout/payment-mock?paymentId=%s&amount=%s&orderId=%s&signature=%s",
                paymentId, amountStr, orderId, signature);
    }

    @Override
    public boolean verifyCallback(Map<String, String> queryParams) {
        String orderId = queryParams.get("orderId");
        String amount = queryParams.get("amount");
        String paymentId = queryParams.get("paymentId");
        String status = queryParams.get("status");
        String signature = queryParams.get("signature");

        if (orderId == null || amount == null || paymentId == null || status == null || signature == null) {
            return false;
        }

        // Format to 2 decimal places to ensure string match
        try {
            amount = new java.math.BigDecimal(amount).setScale(2).toPlainString();
        } catch (Exception e) {
            return false;
        }

        String data = amount + "|" + orderId + "|" + paymentId + "|" + status;
        String computedSignature = calculateHmac(data, hmacSecret);

        return computedSignature.equalsIgnoreCase(signature);
    }

    public String generateSignature(String amount, String orderId, String paymentId, String status) {
        String data = amount + "|" + orderId + "|" + paymentId + "|" + status;
        return calculateHmac(data, hmacSecret);
    }

    private String calculateHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error computing HMAC signature", e);
        }
    }
}
