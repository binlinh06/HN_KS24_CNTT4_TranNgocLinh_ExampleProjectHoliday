package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // IMPORT, EXPORT, ADJUST

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "stock_before")
    private BigDecimal stockBefore;

    @Column(name = "stock_after")
    private BigDecimal stockAfter;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id", columnDefinition = "CHAR(36)")
    private String referenceId;

    @ManyToOne
    @JoinColumn(name = "performed_by_employee_id")
    private EmployeeProfile performedBy;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
