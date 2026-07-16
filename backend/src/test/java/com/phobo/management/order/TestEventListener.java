package com.phobo.management.order;

import com.phobo.management.order.event.OrderStatusChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestEventListener {
    private final List<OrderStatusChangedEvent> events = new ArrayList<>();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(OrderStatusChangedEvent event) {
        events.add(event);
    }

    public List<OrderStatusChangedEvent> getEvents() {
        return events;
    }

    public void clearEvents() {
        events.clear();
    }
}
