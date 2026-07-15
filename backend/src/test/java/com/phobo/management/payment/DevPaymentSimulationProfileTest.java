package com.phobo.management.payment;

import com.phobo.management.payment.controller.DevPaymentSimulationController;
import com.phobo.management.payment.gateway.MockPaymentGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("prod")
public class DevPaymentSimulationProfileTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testDevBeansNotRegisteredInProdProfile() {
        // Assert that DevPaymentSimulationController bean does not exist in prod profile
        boolean hasController = applicationContext.containsBean("devPaymentSimulationController") 
                || !applicationContext.getBeansOfType(DevPaymentSimulationController.class).isEmpty();
        assertTrue(!hasController, "DevPaymentSimulationController bean should NOT be registered in prod profile");

        // Assert that MockPaymentGateway bean does not exist in prod profile
        boolean hasGateway = applicationContext.containsBean("mockPaymentGateway")
                || !applicationContext.getBeansOfType(MockPaymentGateway.class).isEmpty();
        assertTrue(!hasGateway, "MockPaymentGateway bean should NOT be registered in prod profile");
    }
}
