package com.phobo.management.address;

import com.phobo.management.address.dto.AddressRequest;
import com.phobo.management.address.dto.AddressResponse;
import com.phobo.management.address.service.AddressService;
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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    public void testAddressCRUDAndConcurrency() throws InterruptedException {
        User user = userRepository.findByUsername("customer")
                .orElseThrow(() -> new RuntimeException("Test user 'customer' not found"));

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. Clear existing addresses
        transactionTemplate.execute(status -> {
            List<AddressResponse> list = addressService.getAddresses();
            for (AddressResponse addr : list) {
                addressService.deleteAddress(addr.getId());
            }
            return null;
        });

        // 2. Create first address -> should automatically be default
        AddressRequest request1 = AddressRequest.builder()
                .receiverName("Nguyễn Văn A")
                .receiverPhone("0987654321")
                .addressDetail("123 Đường Phở, Hà Nội")
                .addressLabel("Nhà riêng")
                .isDefault(false) // request false but should become true because it is the first
                .build();

        AddressResponse res1 = addressService.createAddress(request1);
        assertNotNull(res1);
        assertTrue(res1.getIsDefault());

        // 3. Create second address -> not default
        AddressRequest request2 = AddressRequest.builder()
                .receiverName("Nguyễn Văn B")
                .receiverPhone("0912345678")
                .addressDetail("456 Đường Bún, Hà Nội")
                .addressLabel("Văn phòng")
                .isDefault(false)
                .build();

        AddressResponse res2 = addressService.createAddress(request2);
        assertNotNull(res2);
        assertFalse(res2.getIsDefault());

        // 4. Update default address
        AddressResponse res2Default = addressService.setDefaultAddress(res2.getId());
        assertTrue(res2Default.getIsDefault());

        // Verify first address is no longer default
        AddressResponse res1Updated = addressService.getAddress(res1.getId());
        assertFalse(res1Updated.getIsDefault());

        // 5. Test concurrency set-default on two addresses
        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final String targetId = (i == 0) ? res1.getId() : res2.getId();
            executor.submit(() -> {
                try {
                    SecurityContext threadContext = SecurityContextHolder.createEmptyContext();
                    threadContext.setAuthentication(auth);
                    SecurityContextHolder.setContext(threadContext);

                    startLatch.await();
                    addressService.setDefaultAddress(targetId);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    SecurityContextHolder.clearContext();
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(0, errorCount.get());

        // Verify exactly one address is default
        SecurityContextHolder.setContext(context);
        transactionTemplate.execute(status -> {
            entityManager.clear();
            List<AddressResponse> list = addressService.getAddresses();
            long defaultCount = list.stream().filter(AddressResponse::getIsDefault).count();
            assertEquals(1, defaultCount);
            return null;
        });

        // 6. Delete default address -> oldest remaining should become default
        List<AddressResponse> currentList = addressService.getAddresses();
        AddressResponse currentDefault = currentList.stream().filter(AddressResponse::getIsDefault).findFirst().orElseThrow();
        AddressResponse remaining = currentList.stream().filter(a -> !a.getIsDefault()).findFirst().orElseThrow();

        addressService.deleteAddress(currentDefault.getId());

        // The remaining one should now be default
        AddressResponse remainingUpdated = addressService.getAddress(remaining.getId());
        assertTrue(remainingUpdated.getIsDefault());

        SecurityContextHolder.clearContext();
    }
}
