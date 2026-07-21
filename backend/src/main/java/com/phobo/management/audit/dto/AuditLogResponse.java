package com.phobo.management.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private String id;
    private String actorUserId;
    private String actorRole;
    private String action;
    private String entityType;
    private String entityId;
    private String result;
    private String summary;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
