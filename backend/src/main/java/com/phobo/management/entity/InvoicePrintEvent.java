package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_print_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePrintEvent {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "printed_by_employee_id", nullable = false)
    private EmployeeProfile printedBy;

    @Column(name = "printed_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime printedAt = LocalDateTime.now();

    @Column(name = "print_reason")
    private String printReason;
}
