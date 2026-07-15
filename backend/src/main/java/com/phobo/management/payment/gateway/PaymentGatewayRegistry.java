package com.phobo.management.payment.gateway;

import com.phobo.management.exception.PaymentException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentGatewayRegistry {

    private final List<PaymentGateway> gateways;

    public PaymentGatewayRegistry(List<PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway getGateway(String provider) {
        return gateways.stream()
                .filter(g -> g.supports(provider))
                .findFirst()
                .orElseThrow(() -> new PaymentException(
                        "Cổng thanh toán '" + provider + "' hiện tại không khả dụng",
                        "PAYMENT_PROVIDER_UNAVAILABLE",
                        HttpStatus.SERVICE_UNAVAILABLE
                ));
    }
}
