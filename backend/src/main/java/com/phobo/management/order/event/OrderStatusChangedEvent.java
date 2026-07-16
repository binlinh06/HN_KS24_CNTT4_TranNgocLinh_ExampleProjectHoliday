package com.phobo.management.order.event;

import com.phobo.management.entity.OrderStatusHistory;
import org.springframework.context.ApplicationEvent;

public class OrderStatusChangedEvent extends ApplicationEvent {
    private final OrderStatusHistory history;

    public OrderStatusChangedEvent(Object source, OrderStatusHistory history) {
        super(source);
        this.history = history;
    }

    public OrderStatusHistory getHistory() {
        return history;
    }
}
