package com.phobo.management.cart.service;

import com.phobo.management.cart.dto.CartItemRequest;
import com.phobo.management.cart.dto.CartResponse;
import com.phobo.management.entity.User;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.security.CustomUserPrincipal;
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

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CartServiceConcurrencyTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    public void testConcurrentAddItemSameConfiguration() throws InterruptedException {
        User user = userRepository.findByUsername("customer")
                .orElseThrow(() -> new RuntimeException("Test user 'customer' not found"));

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        CartItemRequest request = CartItemRequest.builder()
                .productId(UUID.fromString("a1a1a1a1-1111-1111-1111-111111111111"))
                .optionIds(Collections.singletonList(UUID.fromString("d1111111-1111-1111-1111-111111111111")))
                .quantity(1)
                .specialNote("Ít bánh")
                .build();

        // Clear cart first
        SecurityContext mainContext = SecurityContextHolder.createEmptyContext();
        mainContext.setAuthentication(auth);
        SecurityContextHolder.setContext(mainContext);
        cartService.clearCart();
        SecurityContextHolder.clearContext();

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger errorCount = new AtomicInteger(0);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(auth);
                    SecurityContextHolder.setContext(context);

                    // Wait for start signal
                    startLatch.await();

                    cartService.addItem(request);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.out.println("Thread error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    SecurityContextHolder.clearContext();
                    finishLatch.countDown();
                }
            });
        }

        // Release the threads
        startLatch.countDown();
        finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify state
        SecurityContextHolder.setContext(mainContext);
        
        // Force reload from database to avoid caching issues
        transactionTemplate.execute(status -> {
            entityManager.clear(); // Clear L1 cache
            CartResponse cartResponse = cartService.getCart();
            
            System.out.println("TEST_DEBUG: Cart items size: " + cartResponse.getItems().size());
            for (var item : cartResponse.getItems()) {
                System.out.println("TEST_DEBUG: Item: id=" + item.getId() + ", product=" + item.getProduct().getId() 
                    + ", qty=" + item.getQuantity() + ", note=" + item.getSpecialNote() 
                    + ", options=" + item.getSelectedOptions().stream().map(o -> o.getId()).toList());
            }
            
            // Expected: 1 item with quantity 2
            assertEquals(1, cartResponse.getItems().size());
            assertEquals(2, cartResponse.getItems().get(0).getQuantity());
            return null;
        });

        SecurityContextHolder.clearContext();
        assertEquals(0, errorCount.get(), "No thread errors should have occurred");
    }
}
