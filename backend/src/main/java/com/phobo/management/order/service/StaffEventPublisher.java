package com.phobo.management.order.service;

import com.phobo.management.order.event.StaffEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StaffEventPublisher {

    private final StaffEventSubscriptionService subscriptionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStaffEvent(StaffEvent event) {
        log.info("Dispatching StaffEvent [{}] after commit. EventId: {}", event.getEventName(), event.getEventId());
        
        Map<String, Object> message = new HashMap<>();
        message.put("eventId", event.getEventId());
        message.put("eventName", event.getEventName());
        message.put("payload", event.getPayload());

        subscriptionService.publishEvent(message, event.getEventId(), event.getEventName());
    }
}
