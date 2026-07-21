package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "actor_user_id", columnDefinition = "CHAR(36)")
    private String actorUserId;

    @Column(name = "actor_role", nullable = false)
    private String actorRole;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", columnDefinition = "CHAR(36)")
    private String entityId;

    @Column(nullable = false)
    private String result; // SUCCESS, FAILURE, FORBIDDEN

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
