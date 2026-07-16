package com.phobo.management.order.service;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.entity.OrderStatusHistory;
import com.phobo.management.order.event.OrderStatusChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderStatusEventPublisher {

    private final OrderTrackingSubscriptionService subscriptionService;

    public OrderStatusEventPublisher(OrderTrackingSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Listens to status change events only AFTER the transaction has successfully committed
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        OrderStatusHistory history = event.getHistory();
        String orderId = history.getOrder().getId();

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId);
        payload.put("orderCode", history.getOrder().getOrderCode());
        payload.put("currentStatus", history.getNewStatus().name());
        payload.put("statusUpdatedAt", history.getCreatedAt().toString());

        // Use history record ID as unique eventId
        String eventId = history.getId();

        subscriptionService.publishEvent(orderId, payload, eventId, "STATUS_UPDATE");
    }
}
