package com.phobo.management.health.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class HealthService {
    public Map<String, Object> checkHealth() {
        return Map.of(
            "status", "ok",
            "timestamp", java.time.LocalDateTime.now().toString(),
            "phase", "Phase 1 - Scaffold Complete"
        );
    }
}
