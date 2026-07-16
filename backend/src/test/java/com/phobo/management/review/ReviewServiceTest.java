package com.phobo.management.review;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.OrderType;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.RoleCode;
import com.phobo.management.common.enums.UserStatus;
import com.phobo.management.entity.CustomerProfile;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.entity.Role;
import com.phobo.management.entity.User;
import com.phobo.management.entity.Review;
import com.phobo.management.exception.ReviewException;
import com.phobo.management.review.dto.ReviewRequest;
import com.phobo.management.review.dto.ReviewResponse;
import com.phobo.management.review.service.ReviewService;
import com.phobo.management.repository.CustomerProfileRepository;
import com.phobo.management.repository.OrderEntityRepository;
import com.phobo.management.repository.RoleRepository;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.repository.ReviewRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderEntityRepository orderRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ReviewRepository reviewRepository;

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
    public void testReviewSubmissions() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. Setup completed order (HOAN_THANH) for owner
        OrderEntity completedOrder = transactionTemplate.execute(status -> {
            authenticateAsUser("customer");
            CustomerProfile customerProfile = customerProfileRepository.findByUserId(
                userRepository.findByUsername("customer").orElseThrow().getId()
            ).orElseThrow();

            OrderEntity o = OrderEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .customer(customerProfile)
                    .orderCode("REV-COMP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                    .paymentMethod(PaymentMethod.COD)
                    .orderType(OrderType.ONLINE)
                    .totalAmount(new BigDecimal("120000"))
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(new BigDecimal("120000"))
                    .status(OrderStatus.HOAN_THANH)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return orderRepository.save(o);
        });

        assertNotNull(completedOrder);

        // 2. Setup processing order (DANG_CHE_BIEN)
        OrderEntity pendingOrder = transactionTemplate.execute(status -> {
            authenticateAsUser("customer");
            CustomerProfile customerProfile = customerProfileRepository.findByUserId(
                userRepository.findByUsername("customer").orElseThrow().getId()
            ).orElseThrow();

            OrderEntity o = OrderEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .customer(customerProfile)
                    .orderCode("REV-PEND-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                    .paymentMethod(PaymentMethod.COD)
                    .orderType(OrderType.ONLINE)
                    .totalAmount(new BigDecimal("95000"))
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(new BigDecimal("95000"))
                    .status(OrderStatus.DANG_CHE_BIEN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return orderRepository.save(o);
        });

        // 3. Submit valid review
        authenticateAsUser("customer");
        ReviewRequest request = ReviewRequest.builder()
                .rating(5)
                .comment("Phở ngon tuyệt vời, giao hàng nhanh!")
                .build();

        ReviewResponse response = reviewService.createReview(completedOrder.getId(), request);
        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Phở ngon tuyệt vời, giao hàng nhanh!", response.getComment());
        assertEquals("PENDING", response.getModerationStatus().name());

        // 4. Submit duplicate review -> should throw REVIEW_ALREADY_EXISTS
        ReviewRequest request2 = ReviewRequest.builder().rating(4).comment("Trùng lặp").build();
        assertThrows(ReviewException.class, () -> {
            reviewService.createReview(completedOrder.getId(), request2);
        });

        // 5. Submit review for pending order -> should throw ORDER_NOT_COMPLETED
        assertThrows(ReviewException.class, () -> {
            reviewService.createReview(pendingOrder.getId(), request2);
        });

        // 6. Submit review as non-owner -> should throw ORDER_FORBIDDEN
        assertThrows(ReviewException.class, () -> {
            authenticateAsUser("customer_two");
            reviewService.createReview(completedOrder.getId(), request2);
        });
    }

    @Test
    public void testConcurrentReviewSubmissions() throws InterruptedException {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // Setup a completed order that is NOT yet reviewed
        OrderEntity order = transactionTemplate.execute(status -> {
            authenticateAsUser("customer");
            CustomerProfile customerProfile = customerProfileRepository.findByUserId(
                userRepository.findByUsername("customer").orElseThrow().getId()
            ).orElseThrow();

            OrderEntity o = OrderEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .customer(customerProfile)
                    .orderCode("REV-CONC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                    .paymentMethod(PaymentMethod.COD)
                    .orderType(OrderType.ONLINE)
                    .totalAmount(new BigDecimal("150000"))
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(new BigDecimal("150000"))
                    .status(OrderStatus.HOAN_THANH)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return orderRepository.save(o);
        });

        assertNotNull(order);

        int numberOfThreads = 2;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            service.submit(() -> {
                try {
                    latch.await(); // wait for start signal

                    authenticateAsUser("customer");

                    ReviewRequest req = ReviewRequest.builder()
                            .rating(4)
                            .comment("Đồng thời thread " + threadNum)
                            .build();

                    transactionTemplate.execute(status -> {
                        reviewService.createReview(order.getId(), req);
                        return null;
                    });

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // start threads simultaneously
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed);

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        List<Review> storedReviews = reviewRepository.findAll().stream()
                .filter(r -> r.getOrder().getId().equals(order.getId()))
                .toList();
        assertEquals(1, storedReviews.size());

        service.shutdown();
    }
}
