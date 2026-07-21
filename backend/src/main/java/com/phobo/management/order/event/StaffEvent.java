package com.phobo.management.order.event;

import org.springframework.context.ApplicationEvent;

public class StaffEvent extends ApplicationEvent {
    private final String eventId;
    private final String eventName;
    private final Object payload;

    public StaffEvent(Object source, String eventId, String eventName, Object payload) {
        super(source);
        this.eventId = eventId;
        this.eventName = eventName;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public Object getPayload() {
        return payload;
    }
}
