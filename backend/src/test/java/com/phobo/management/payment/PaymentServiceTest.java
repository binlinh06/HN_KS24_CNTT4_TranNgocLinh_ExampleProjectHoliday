package com.phobo.management.payment;

import com.phobo.management.address.dto.AddressRequest;
import com.phobo.management.address.dto.AddressResponse;
import com.phobo.management.address.service.AddressService;
import com.phobo.management.cart.dto.CartItemRequest;
import com.phobo.management.cart.service.CartService;
import com.phobo.management.checkout.dto.CheckoutPreviewRequest;
import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.PaymentStatus;
import com.phobo.management.entity.User;
import com.phobo.management.order.dto.OrderResponse;
import com.phobo.management.order.service.OrderService;
import com.phobo.management.payment.dto.PaymentResponse;
import com.phobo.management.payment.service.PaymentService;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.security.CustomUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.phobo.management.repository.CustomerProfileRepository customerProfileRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String calculateHmac(String data, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPaymentFlowAndCallback() {
        User user = userRepository.findByUsername("customer")
                .orElseThrow(() -> new RuntimeException("Test user 'customer' not found"));

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. Setup address
        AddressResponse address = transactionTemplate.execute(status -> {
            List<AddressResponse> list = addressService.getAddresses();
            if (!list.isEmpty()) {
                return list.get(0);
            }
            AddressRequest req = AddressRequest.builder()
                    .receiverName("Khách Hàng A")
                    .receiverPhone("0987654321")
                    .addressDetail("123 Phố Phở, Hà Nội")
                    .addressLabel("Nhà")
                    .isDefault(true)
                    .build();
            return addressService.createAddress(req);
        });

        // 2. Setup cart
        transactionTemplate.execute(status -> {
            cartService.clearCart();
            CartItemRequest itemReq = CartItemRequest.builder()
                    .productId(UUID.fromString("a1a1a1a1-1111-1111-1111-111111111111"))
                    .optionIds(Collections.singletonList(UUID.fromString("d1111111-1111-1111-1111-111111111111")))
                    .quantity(1)
                    .specialNote("Ít bánh")
                    .build();
            cartService.addItem(itemReq);
            return null;
        });

        // 3. Create ONLINE order
        CheckoutPreviewRequest checkoutReq = CheckoutPreviewRequest.builder()
                .addressId(address.getId())
                .paymentMethod("ONLINE")
                .customerNote("Note")
                .build();

        String idempotencyKey1 = UUID.randomUUID().toString();
        OrderResponse orderRes = orderService.checkoutOrder(checkoutReq, idempotencyKey1);
        assertNotNull(orderRes);

        // 4. Initiate payment
        String idempotencyKey2 = UUID.randomUUID().toString();
        PaymentResponse paymentRes = paymentService.initiatePayment(orderRes.getOrderId(), idempotencyKey2);
        assertNotNull(paymentRes);
        assertEquals(PaymentStatus.PENDING, paymentRes.getPaymentStatus());
        assertNotNull(paymentRes.getPaymentUrl());

        // 5. Test callback with invalid signature
        Map<String, String> invalidParams = new HashMap<>();
        invalidParams.put("orderId", orderRes.getOrderId());
        invalidParams.put("amount", orderRes.getFinalAmount().setScale(2).toPlainString());
        invalidParams.put("paymentId", paymentRes.getPaymentId());
        invalidParams.put("status", "SUCCESS");
        invalidParams.put("signature", "invalid_signature");

        assertThrows(RuntimeException.class, () -> paymentService.processCallback("MOCK", invalidParams));

        // 6. Test callback with valid signature
        String amountStr = orderRes.getFinalAmount().setScale(2).toPlainString();
        String data = amountStr + "|" + orderRes.getOrderId() + "|" + paymentRes.getPaymentId() + "|SUCCESS";
        String validSignature = calculateHmac(data, "mock_secret_key");

        Map<String, String> validParams = new HashMap<>();
        validParams.put("orderId", orderRes.getOrderId());
        validParams.put("amount", amountStr);
        validParams.put("paymentId", paymentRes.getPaymentId());
        validParams.put("status", "SUCCESS");
        validParams.put("signature", validSignature);

        PaymentResponse callbackRes = paymentService.processCallback("MOCK", validParams);
        assertEquals(PaymentStatus.SUCCESS, callbackRes.getPaymentStatus());

        // Verify Order status updated to DA_XAC_NHAN
        OrderResponse finalOrder = orderService.getOrderDetails(orderRes.getOrderId());
        assertEquals(OrderStatus.DA_XAC_NHAN, finalOrder.getStatus());

        // 7. SUCCESS is terminal state: subsequent FAILED callback should be ignored
        String dataFailed = amountStr + "|" + orderRes.getOrderId() + "|" + paymentRes.getPaymentId() + "|FAILED";
        String validSignatureFailed = calculateHmac(dataFailed, "mock_secret_key");

        Map<String, String> failedParams = new HashMap<>();
        failedParams.put("orderId", orderRes.getOrderId());
        failedParams.put("amount", amountStr);
        failedParams.put("paymentId", paymentRes.getPaymentId());
        failedParams.put("status", "FAILED");
        failedParams.put("signature", validSignatureFailed);

        PaymentResponse callbackResAfter = paymentService.processCallback("MOCK", failedParams);
        // Status should still be SUCCESS
        assertEquals(PaymentStatus.SUCCESS, callbackResAfter.getPaymentStatus());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void testPaymentSimulationAndHarden() {
        User user = userRepository.findByUsername("customer")
                .orElseThrow(() -> new RuntimeException("Test user 'customer' not found"));

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // Setup addresses
        AddressResponse address = transactionTemplate.execute(status -> {
            List<AddressResponse> list = addressService.getAddresses();
            if (!list.isEmpty()) {
                return list.get(0);
            }
            AddressRequest req = AddressRequest.builder()
                    .receiverName("Khách Hàng A")
                    .receiverPhone("0987654321")
                    .addressDetail("123 Phố Phở, Hà Nội")
                    .addressLabel("Nhà")
                    .isDefault(true)
                    .build();
            return addressService.createAddress(req);
        });

        // Setup cart
        transactionTemplate.execute(status -> {
            cartService.clearCart();
            CartItemRequest itemReq = CartItemRequest.builder()
                    .productId(UUID.fromString("a1a1a1a1-1111-1111-1111-111111111111"))
                    .optionIds(Collections.singletonList(UUID.fromString("d1111111-1111-1111-1111-111111111111")))
                    .quantity(1)
                    .specialNote("Ít bánh")
                    .build();
            cartService.addItem(itemReq);
            return null;
        });

        // 1. SUCCESS simulation
        CheckoutPreviewRequest checkoutReq = CheckoutPreviewRequest.builder()
                .addressId(address.getId())
                .paymentMethod("ONLINE")
                .customerNote("Note")
                .build();

        OrderResponse orderRes1 = orderService.checkoutOrder(checkoutReq, UUID.randomUUID().toString());
        PaymentResponse paymentRes1 = paymentService.initiatePayment(orderRes1.getOrderId(), UUID.randomUUID().toString());
        assertEquals(PaymentStatus.PENDING, paymentRes1.getPaymentStatus());

        // Perform simulation
        PaymentResponse simulateSuccess = paymentService.simulateMockPayment(paymentRes1.getPaymentId(), "SUCCESS");
        assertEquals(PaymentStatus.SUCCESS, simulateSuccess.getPaymentStatus());

        // Verify Order status is now DA_XAC_NHAN
        OrderResponse orderRes1Final = orderService.getOrderDetails(orderRes1.getOrderId());
        assertEquals(OrderStatus.DA_XAC_NHAN, orderRes1Final.getStatus());

        // 2. SUCCESS is terminal
        assertThrows(Exception.class, () -> paymentService.simulateMockPayment(paymentRes1.getPaymentId(), "FAILED"));

        // 3. Duplicate simulation is idempotent
        PaymentResponse simulateSuccessDup = paymentService.simulateMockPayment(paymentRes1.getPaymentId(), "SUCCESS");
        assertEquals(PaymentStatus.SUCCESS, simulateSuccessDup.getPaymentStatus());

        // 4. FAILED simulation
        transactionTemplate.execute(status -> {
            cartService.clearCart();
            CartItemRequest itemReq = CartItemRequest.builder()
                    .productId(UUID.fromString("a1a1a1a1-1111-1111-1111-111111111111"))
                    .quantity(1)
                    .build();
            cartService.addItem(itemReq);
            return null;
        });

        OrderResponse orderRes2 = orderService.checkoutOrder(checkoutReq, UUID.randomUUID().toString());
        PaymentResponse paymentRes2 = paymentService.initiatePayment(orderRes2.getOrderId(), UUID.randomUUID().toString());

        PaymentResponse simulateFailed = paymentService.simulateMockPayment(paymentRes2.getPaymentId(), "FAILED");
        assertEquals(PaymentStatus.FAILED, simulateFailed.getPaymentStatus());

        // 5. CANCELED simulation
        transactionTemplate.execute(status -> {
            cartService.clearCart();
            CartItemRequest itemReq = CartItemRequest.builder()
                    .productId(UUID.fromString("a1a1a1a1-1111-1111-1111-111111111111"))
                    .quantity(1)
                    .build();
            cartService.addItem(itemReq);
            return null;
        });
        OrderResponse orderRes3 = orderService.checkoutOrder(checkoutReq, UUID.randomUUID().toString());
        PaymentResponse paymentRes3 = paymentService.initiatePayment(orderRes3.getOrderId(), UUID.randomUUID().toString());

        PaymentResponse simulateCancelled = paymentService.simulateMockPayment(paymentRes3.getPaymentId(), "CANCELED");
        assertEquals(PaymentStatus.FAILED, simulateCancelled.getPaymentStatus());

        // 6. EXPIRED simulation
        transactionTemplate.execute(status -> {
            cartService.clearCart();
            CartItemRequest itemReq = CartItemRequest.builder()
                    .productId(UUID.fromString("a1a1a1a1-1111-1111-1111-111111111111"))
                    .quantity(1)
                    .build();
            cartService.addItem(itemReq);
            return null;
        });
        OrderResponse orderRes4 = orderService.checkoutOrder(checkoutReq, UUID.randomUUID().toString());
        PaymentResponse paymentRes4 = paymentService.initiatePayment(orderRes4.getOrderId(), UUID.randomUUID().toString());

        PaymentResponse simulateExpired = paymentService.simulateMockPayment(paymentRes4.getPaymentId(), "EXPIRED");
        assertEquals(PaymentStatus.FAILED, simulateExpired.getPaymentStatus());

        // 7. Invalid status
        assertThrows(Exception.class, () -> paymentService.simulateMockPayment(paymentRes2.getPaymentId(), "INVALID"));

        // 8. Simulation for payment belonging to different customer
        User customer2User = transactionTemplate.execute(status -> {
            User c2 = userRepository.findByUsername("customer2").orElseGet(() -> {
                String userId = UUID.randomUUID().toString();
                jdbcTemplate.update(
                    "INSERT INTO users (id, username, password_hash, email, phone, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())",
                    userId, "customer2", "password_hash", "customer2@example.com", "0987654325", "ACTIVE"
                );
                
                // Map roles
                for (com.phobo.management.entity.Role role : user.getRoles()) {
                    jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", userId, role.getId());
                }
                
                return userRepository.findById(userId).orElseThrow();
            });
            customerProfileRepository.findByUserId(c2.getId()).orElseGet(() -> {
                com.phobo.management.entity.CustomerProfile p = com.phobo.management.entity.CustomerProfile.builder()
                        .id(UUID.randomUUID().toString())
                        .user(c2)
                        .fullName("Customer 2")
                        .loyaltyPoints(0)
                        .build();
                return customerProfileRepository.save(p);
            });
            return c2;
        });

        CustomUserPrincipal principal2 = new CustomUserPrincipal(customer2User);
        Authentication auth2 = new UsernamePasswordAuthenticationToken(principal2, null, principal2.getAuthorities());
        SecurityContext context2 = SecurityContextHolder.createEmptyContext();
        context2.setAuthentication(auth2);
        SecurityContextHolder.setContext(context2);

        // Try to access paymentRes2 (which belongs to "customer") under customer2 context -> should fail
        assertThrows(Exception.class, () -> paymentService.simulateMockPayment(paymentRes2.getPaymentId(), "SUCCESS"));

        // 9. Callback amount mismatch
        SecurityContextHolder.setContext(context); // back to "customer"
        Map<String, String> mismatchParams = new HashMap<>();
        mismatchParams.put("orderId", orderRes2.getOrderId());
        mismatchParams.put("amount", "999999.00");
        mismatchParams.put("paymentId", paymentRes2.getPaymentId());
        mismatchParams.put("status", "SUCCESS");
        String dataMismatch = "999999.00|" + orderRes2.getOrderId() + "|" + paymentRes2.getPaymentId() + "|SUCCESS";
        mismatchParams.put("signature", calculateHmac(dataMismatch, "mock_secret_key"));
        assertThrows(Exception.class, () -> paymentService.processCallback("MOCK", mismatchParams));

        // 10. Return URL spoofing (invalid signature)
        transactionTemplate.execute(status -> {
            cartService.clearCart();
            CartItemRequest itemReq = CartItemRequest.builder()
                    .productId(UUID.fromString("a1a1a1a1-1111-1111-1111-111111111111"))
                    .quantity(1)
                    .build();
            cartService.addItem(itemReq);
            return null;
        });
        OrderResponse orderRes5 = orderService.checkoutOrder(checkoutReq, UUID.randomUUID().toString());
        PaymentResponse paymentRes5 = paymentService.initiatePayment(orderRes5.getOrderId(), UUID.randomUUID().toString());

        Map<String, String> spoofParams = new HashMap<>();
        spoofParams.put("orderId", orderRes5.getOrderId());
        spoofParams.put("amount", orderRes5.getFinalAmount().setScale(2).toPlainString());
        spoofParams.put("paymentId", paymentRes5.getPaymentId());
        spoofParams.put("status", "SUCCESS");
        spoofParams.put("signature", "fake_signature_hash");
        
        assertThrows(Exception.class, () -> paymentService.processCallback("MOCK", spoofParams));

        // Verify status remains PENDING
        PaymentResponse checkRes = paymentService.getPaymentDetails(orderRes5.getOrderId());
        assertEquals(PaymentStatus.PENDING, checkRes.getPaymentStatus());

        SecurityContextHolder.clearContext();
    }
}
