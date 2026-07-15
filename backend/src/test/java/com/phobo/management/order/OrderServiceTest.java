package com.phobo.management.order;

import com.phobo.management.address.dto.AddressRequest;
import com.phobo.management.address.dto.AddressResponse;
import com.phobo.management.address.service.AddressService;
import com.phobo.management.cart.dto.CartItemRequest;
import com.phobo.management.cart.dto.CartResponse;
import com.phobo.management.cart.service.CartService;
import com.phobo.management.checkout.dto.CheckoutPreviewRequest;
import com.phobo.management.checkout.service.CheckoutService;
import com.phobo.management.entity.User;
import com.phobo.management.entity.Voucher;
import com.phobo.management.exception.OrderException;
import com.phobo.management.order.dto.OrderResponse;
import com.phobo.management.order.service.OrderService;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.repository.VoucherRepository;
import com.phobo.management.security.CustomUserPrincipal;
import com.phobo.management.common.enums.OrderStatus;
import jakarta.persistence.EntityManager;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    public void testOrderCheckoutFlow() {
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
                    .quantity(2)
                    .specialNote("Ít hành")
                    .build();
            cartService.addItem(itemReq);
            return null;
        });

        // 3. Setup active voucher with usage limit
        String voucherCode = "PHO50_" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        transactionTemplate.execute(status -> {
            Voucher voucher = Voucher.builder()
                    .id(UUID.randomUUID().toString())
                    .code(voucherCode)
                    .discountType("FIXED_AMOUNT")
                    .discountValue(BigDecimal.valueOf(10000))
                    .minOrderValue(BigDecimal.valueOf(20000))
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusDays(2))
                    .usageLimit(10)
                    .usedCount(0)
                    .isActive(true)
                    .build();
            voucherRepository.save(voucher);
            return null;
        });

        // Apply voucher to cart
        transactionTemplate.execute(status -> {
            cartService.applyVoucher(voucherCode);
            return null;
        });

        // 4. Test Checkout Preview
        CheckoutPreviewRequest previewReq = CheckoutPreviewRequest.builder()
                .addressId(address.getId())
                .paymentMethod("COD")
                .customerNote("Đũa dùng 1 lần")
                .build();

        // Verify checkout preview calculates properly without writing
        // Since we don't inject CheckoutService directly in the test to keep it minimal,
        // we can just directly run checkoutOrder
        String idempotencyKey1 = UUID.randomUUID().toString();
        OrderResponse orderRes = orderService.checkoutOrder(previewReq, idempotencyKey1);

        assertNotNull(orderRes);
        assertNotNull(orderRes.getOrderCode());
        assertEquals("COD", orderRes.getPaymentMethod().name());
        assertEquals("PENDING", orderRes.getPaymentStatus().name());
        assertEquals(OrderStatus.CHO_XAC_NHAN, orderRes.getStatus());

        // Verify cart is cleared
        CartResponse cartRes = cartService.getCart();
        assertTrue(cartRes.getItems().isEmpty());
        assertNull(cartRes.getVoucher());

        // Verify voucher usedCount was incremented by 1
        Voucher voucherUpdated = voucherRepository.findByCodeIgnoreCaseAndDeletedAtIsNull(voucherCode).orElseThrow();
        assertEquals(1, voucherUpdated.getUsedCount());

        // 5. Test Idempotency: same key + same payload returns same result
        OrderResponse orderResDup = orderService.checkoutOrder(previewReq, idempotencyKey1);
        assertEquals(orderRes.getOrderId(), orderResDup.getOrderId());
        assertEquals(orderRes.getOrderCode(), orderResDup.getOrderCode());

        // 6. Test Idempotency: same key + different payload returns IDEMPOTENCY_KEY_REUSED
        CheckoutPreviewRequest previewReqDiff = CheckoutPreviewRequest.builder()
                .addressId(address.getId())
                .paymentMethod("ONLINE")
                .customerNote("Đũa dùng 1 lần")
                .build();

        assertThrows(OrderException.class, () -> orderService.checkoutOrder(previewReqDiff, idempotencyKey1));

        SecurityContextHolder.clearContext();
    }
}
