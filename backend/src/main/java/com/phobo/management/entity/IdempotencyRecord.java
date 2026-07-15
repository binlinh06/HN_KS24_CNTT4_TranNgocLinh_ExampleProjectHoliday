package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records", uniqueConstraints = {
    @UniqueConstraint(name = "uk_idempotency", columnNames = {"customer_id", "operation", "idempotency_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @Column(name = "idempotency_key", columnDefinition = "CHAR(36)", nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String operation;

    @Column(name = "request_hash", columnDefinition = "CHAR(64)", nullable = false)
    private String requestHash;

    @Column(nullable = false)
    private String status; // 'PROCESSING', 'COMPLETED'

    @Column(name = "response_body", columnDefinition = "LONGTEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
