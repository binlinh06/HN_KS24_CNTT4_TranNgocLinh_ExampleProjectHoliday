package com.phobo.management.order.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class OrderTrackingSubscriptionService {

    // Finite timeout for SSE connections: 15 minutes (900,000 ms), matching access token duration
    public static final long SSE_TIMEOUT = 900000L;
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "sse-heartbeat-thread");
        thread.setDaemon(true);
        return thread;
    });

    public OrderTrackingSubscriptionService() {
        // Start heartbeat periodic task every 30 seconds
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeats, 30, 30, TimeUnit.SECONDS);
    }

    public SseEmitter subscribe(String orderId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emittersMap.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(orderId, emitter));
        emitter.onTimeout(() -> removeEmitter(orderId, emitter));
        emitter.onError((e) -> removeEmitter(orderId, emitter));

        return emitter;
    }

    public void publishEvent(String orderId, Object data, String eventId, String name) {
        List<SseEmitter> list = emittersMap.get(orderId);
        if (list == null || list.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .id(eventId)
                        .name(name)
                        .data(data)
                        .build());
            } catch (IOException | IllegalStateException e) {
                removeEmitter(orderId, emitter);
            }
        }
    }

    private void removeEmitter(String orderId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emittersMap.get(orderId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emittersMap.remove(orderId);
            }
        }
    }

    private void sendHeartbeats() {
        for (Map.Entry<String, CopyOnWriteArrayList<SseEmitter>> entry : emittersMap.entrySet()) {
            String orderId = entry.getKey();
            List<SseEmitter> list = entry.getValue();
            for (SseEmitter emitter : list) {
                try {
                    // Send an empty comment event as heartbeat to keep connection alive
                    emitter.send(SseEmitter.event().comment("heartbeat").build());
                } catch (IOException | IllegalStateException e) {
                    removeEmitter(orderId, emitter);
                }
            }
        }
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        heartbeatExecutor.shutdown();
    }
}
