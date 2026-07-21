package com.phobo.management.audit.service;

import com.phobo.management.audit.dto.AuditLogResponse;
import com.phobo.management.entity.AuditLog;
import com.phobo.management.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String actorUserId, String actorRole, String action, String entityType,
                          String entityId, String result, String summary, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .actorUserId(actorUserId)
                    .actorRole(actorRole != null ? actorRole : "SYSTEM")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .result(result)
                    .summary(summary)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to write audit log entry: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(String actorUserId, String action, String entityType,
                                               LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.findWithFilters(actorUserId, action, entityType, from, to, pageable)
                .map(this::mapToResponse);
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorUserId(log.getActorUserId())
                .actorRole(log.getActorRole())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .result(log.getResult())
                .summary(log.getSummary())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
