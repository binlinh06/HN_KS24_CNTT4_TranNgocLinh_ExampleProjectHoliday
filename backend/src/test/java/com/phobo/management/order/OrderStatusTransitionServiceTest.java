package com.phobo.management.order;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.OrderType;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.entity.CustomerProfile;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.entity.OrderStatusHistory;
import com.phobo.management.order.service.OrderStatusTransitionService;
import com.phobo.management.exception.OrderException;
import com.phobo.management.repository.CustomerProfileRepository;
import com.phobo.management.repository.OrderEntityRepository;
import com.phobo.management.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderStatusTransitionServiceTest {

    @Autowired
    private OrderStatusTransitionService transitionService;

    @Autowired
    private OrderEntityRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TestEventListener testEventListener;

    @Test
    public void testStatusTransitionsAndIdempotency() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // Setup test data
        OrderEntity order = transactionTemplate.execute(status -> {
            CustomerProfile customer = customerProfileRepository.findAll().get(0);
            OrderEntity o = OrderEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .customer(customer)
                    .orderCode("TEST-TRANS-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                    .paymentMethod(PaymentMethod.COD)
                    .orderType(OrderType.ONLINE)
                    .totalAmount(new BigDecimal("100000"))
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(new BigDecimal("100000"))
                    .status(OrderStatus.CHO_XAC_NHAN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            OrderEntity saved = orderRepository.save(o);

            // Record initial history
            transitionService.recordInitialHistory(saved, "CHECKOUT");
            return saved;
        });

        assertNotNull(order);
        assertEquals(OrderStatus.CHO_XAC_NHAN, order.getStatus());

        // Verify initial history
        List<OrderStatusHistory> initialHistory = historyRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        assertEquals(1, initialHistory.size());
        assertNull(initialHistory.get(0).getPreviousStatus());
        assertEquals(OrderStatus.CHO_XAC_NHAN, initialHistory.get(0).getNewStatus());
        assertEquals("CHECKOUT", initialHistory.get(0).getChangeSource());

        // Reset listener events
        testEventListener.clearEvents();

        // 1. Transition to DA_XAC_NHAN (valid)
        transactionTemplate.execute(status -> {
            OrderEntity o = orderRepository.findById(order.getId()).orElseThrow();
            transitionService.transitionStatus(o, OrderStatus.DA_XAC_NHAN, null, "SYSTEM", "TEST", "Xác nhận");
            return null;
        });

        OrderEntity updatedOrder1 = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DA_XAC_NHAN, updatedOrder1.getStatus());

        List<OrderStatusHistory> history1 = historyRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        assertEquals(2, history1.size());
        OrderStatusHistory initHistory = history1.stream().filter(h -> h.getNewStatus() == OrderStatus.CHO_XAC_NHAN).findFirst().orElseThrow();
        OrderStatusHistory nextHistory = history1.stream().filter(h -> h.getNewStatus() == OrderStatus.DA_XAC_NHAN).findFirst().orElseThrow();
        assertNull(initHistory.getPreviousStatus());
        assertEquals(OrderStatus.CHO_XAC_NHAN, nextHistory.getPreviousStatus());
        assertEquals(OrderStatus.DA_XAC_NHAN, nextHistory.getNewStatus());

        // Verify event was captured (AFTER_COMMIT)
        assertEquals(1, testEventListener.getEvents().size());
        assertEquals(OrderStatus.DA_XAC_NHAN, testEventListener.getEvents().get(0).getHistory().getNewStatus());

        // 2. Transition again to same status (idempotent check)
        transactionTemplate.execute(status -> {
            OrderEntity o = orderRepository.findById(order.getId()).orElseThrow();
            transitionService.transitionStatus(o, OrderStatus.DA_XAC_NHAN, null, "SYSTEM", "TEST", "Xác nhận lại");
            return null;
        });

        // No new history or events should be added
        List<OrderStatusHistory> history2 = historyRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        assertEquals(2, history2.size());
        assertEquals(1, testEventListener.getEvents().size());

        // 3. Invalid transition (DA_XAC_NHAN -> HOAN_THANH is invalid, must go through che bien -> giao)
        assertThrows(OrderException.class, () -> {
            transactionTemplate.execute(status -> {
                OrderEntity o = orderRepository.findById(order.getId()).orElseThrow();
                transitionService.transitionStatus(o, OrderStatus.HOAN_THANH, null, "SYSTEM", "TEST", "Hoàn thành sai");
                return null;
            });
        });

        // 4. Test transaction rollback -> event should NOT be published
        testEventListener.clearEvents();
        try {
            transactionTemplate.execute(status -> {
                OrderEntity o = orderRepository.findById(order.getId()).orElseThrow();
                transitionService.transitionStatus(o, OrderStatus.DANG_CHE_BIEN, null, "SYSTEM", "TEST", "Chế biến");
                status.setRollbackOnly(); // force rollback
                return null;
            });
        } catch (Exception ignored) {}

        // Verify status remains unchanged, history not written, event NOT published
        OrderEntity afterRollback = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DA_XAC_NHAN, afterRollback.getStatus());
        assertEquals(0, testEventListener.getEvents().size());
    }
}
