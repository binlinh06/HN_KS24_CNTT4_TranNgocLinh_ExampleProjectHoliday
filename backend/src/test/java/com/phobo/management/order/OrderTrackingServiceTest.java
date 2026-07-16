package com.phobo.management.order;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.OrderType;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.RoleCode;
import com.phobo.management.common.enums.UserStatus;
import com.phobo.management.entity.CustomerProfile;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.entity.Role;
import com.phobo.management.entity.User;
import com.phobo.management.exception.OrderException;
import com.phobo.management.order.dto.OrderTrackingResponse;
import com.phobo.management.order.service.OrderService;
import com.phobo.management.order.service.OrderTrackingSubscriptionService;
import com.phobo.management.repository.CustomerProfileRepository;
import com.phobo.management.repository.OrderEntityRepository;
import com.phobo.management.repository.RoleRepository;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderTrackingServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderTrackingSubscriptionService subscriptionService;

    @Autowired
    private OrderEntityRepository orderRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private void authenticateAsUser(String username) {
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            Role customerRole = roleRepository.findByCode(RoleCode.CUSTOMER).orElseGet(() -> {
                Role r = Role.builder()
                        .id("d2b58ea1-cf33-4f9e-be08-591b920bfd65")
                        .code(RoleCode.CUSTOMER)
                        .name("Khách hàng")
                        .build();
                return roleRepository.save(r);
            });

            User u = User.builder()
                    .id(UUID.randomUUID().toString())
                    .username(username)
                    .email(username + "@example.com")
                    .phone("09" + UUID.randomUUID().toString().substring(0, 8).replaceAll("[^0-9]", "9"))
                    .passwordHash("password")
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .roles(Set.of(customerRole))
                    .build();
            User savedUser = userRepository.save(u);

            CustomerProfile cp = CustomerProfile.builder()
                    .id(UUID.randomUUID().toString())
                    .user(savedUser)
                    .fullName(username + " Full Name")
                    .loyaltyPoints(100)
                    .build();
            customerProfileRepository.save(cp);

            return savedUser;
        });

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testOrderTrackingOwnershipAndSSE() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. Create order for customer 'customer' (seeded or created dynamically)
        OrderEntity order = transactionTemplate.execute(status -> {
            authenticateAsUser("customer");
            CustomerProfile customerProfile = customerProfileRepository.findByUserId(
                userRepository.findByUsername("customer").orElseThrow().getId()
            ).orElseThrow();

            OrderEntity o = OrderEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .customer(customerProfile)
                    .orderCode("TRACK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                    .paymentMethod(PaymentMethod.COD)
                    .orderType(OrderType.ONLINE)
                    .totalAmount(new BigDecimal("85000"))
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(new BigDecimal("85000"))
                    .status(OrderStatus.CHO_XAC_NHAN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return orderRepository.save(o);
        });

        assertNotNull(order);

        // 2. Query tracking as 'customer' (owner) -> should succeed
        authenticateAsUser("customer");
        OrderTrackingResponse tracking = orderService.getOrderTracking(order.getId());
        assertNotNull(tracking);
        assertEquals(order.getId(), tracking.getOrderId());
        assertEquals(OrderStatus.CHO_XAC_NHAN, tracking.getCurrentStatus());

        // 3. Query tracking as 'customer_two' (non-owner) -> should fail (403/Forbidden)
        authenticateAsUser("customer_two");
        assertThrows(OrderException.class, () -> {
            orderService.getOrderTracking(order.getId());
        });

        // 4. Test SSE subscription
        authenticateAsUser("customer");
        SseEmitter emitter = subscriptionService.subscribe(order.getId());
        assertNotNull(emitter);

        // Clean up connection
        emitter.complete();
    }
}
