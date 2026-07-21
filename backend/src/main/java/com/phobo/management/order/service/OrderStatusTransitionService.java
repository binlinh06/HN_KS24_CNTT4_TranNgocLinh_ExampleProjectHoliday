package com.phobo.management.order.service;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.entity.OrderStatusHistory;
import com.phobo.management.exception.OrderException;
import com.phobo.management.order.event.OrderStatusChangedEvent;
import com.phobo.management.repository.OrderEntityRepository;
import com.phobo.management.repository.OrderStatusHistoryRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderStatusTransitionService {

    private final OrderEntityRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Transition matrix defining allowed next states from a current state
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITION_MATRIX = new HashMap<>();

    static {
        TRANSITION_MATRIX.put(OrderStatus.CHO_XAC_NHAN, EnumSet.of(OrderStatus.DA_XAC_NHAN, OrderStatus.DA_HUY));
        TRANSITION_MATRIX.put(OrderStatus.DA_XAC_NHAN, EnumSet.of(OrderStatus.DANG_CHE_BIEN, OrderStatus.DA_HUY));
        TRANSITION_MATRIX.put(OrderStatus.DANG_CHE_BIEN, EnumSet.of(OrderStatus.DANG_GIAO, OrderStatus.DANG_PHUC_VU, OrderStatus.HOAN_THANH, OrderStatus.DA_HUY));
        TRANSITION_MATRIX.put(OrderStatus.DANG_GIAO, EnumSet.of(OrderStatus.HOAN_THANH, OrderStatus.DA_HUY));
        TRANSITION_MATRIX.put(OrderStatus.DANG_PHUC_VU, EnumSet.of(OrderStatus.HOAN_THANH, OrderStatus.DA_HUY));
        // Terminal states have no transitions
        TRANSITION_MATRIX.put(OrderStatus.HOAN_THANH, EnumSet.noneOf(OrderStatus.class));
        TRANSITION_MATRIX.put(OrderStatus.DA_HUY, EnumSet.noneOf(OrderStatus.class));
    }

    public OrderStatusTransitionService(
            OrderEntityRepository orderRepository,
            OrderStatusHistoryRepository historyRepository,
            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void transitionStatus(
            OrderEntity order,
            OrderStatus newStatus,
            String changedByUserId,
            String changedByRole,
            String changeSource,
            String reason) {

        OrderStatus currentStatus = order.getStatus();

        // 1. If new status is same as current status, ignore (idempotent)
        if (currentStatus == newStatus) {
            return;
        }

        // 2. Validate transition
        Set<OrderStatus> allowedNextStates = TRANSITION_MATRIX.get(currentStatus);
        if (allowedNextStates == null || !allowedNextStates.contains(newStatus)) {
            throw new OrderException(
                    String.format("Không thể chuyển trạng thái đơn hàng từ %s sang %s", currentStatus, newStatus),
                    "INVALID_ORDER_STATUS",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3. Update Order entity
        order.setStatus(newStatus);
        order.setStatusUpdatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Force version increment & save
        OrderEntity savedOrder = orderRepository.save(order);

        // 4. Record history entry
        OrderStatusHistory history = OrderStatusHistory.builder()
                .id(UUID.randomUUID().toString())
                .order(savedOrder)
                .previousStatus(currentStatus)
                .newStatus(newStatus)
                .changedByUserId(changedByUserId)
                .changedByRole(changedByRole)
                .changeSource(changeSource)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);

        // 5. Publish event for real-time SSE delivery (handled AFTER_COMMIT)
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, history));
    }

    @Transactional
    public void recordInitialHistory(OrderEntity order, String changeSource) {
        recordInitialHistory(order, changeSource, 
                order.getCustomer() != null ? order.getCustomer().getUser().getId() : null, 
                "CUSTOMER", "Khởi tạo đơn hàng");
    }

    @Transactional
    public void recordInitialHistory(OrderEntity order, String changeSource, String changedByUserId, String changedByRole, String reason) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .id(UUID.randomUUID().toString())
                .order(order)
                .previousStatus(null)
                .newStatus(order.getStatus())
                .changedByUserId(changedByUserId)
                .changedByRole(changedByRole)
                .changeSource(changeSource)
                .reason(reason)
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now())
                .build();
        historyRepository.save(history);

        order.setStatusUpdatedAt(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now());
        orderRepository.save(order);
    }
}
