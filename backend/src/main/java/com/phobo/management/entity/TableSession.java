package com.phobo.management.entity;

import com.phobo.management.common.enums.TableSessionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableSession {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableSessionStatus status;

    @ManyToOne
    @JoinColumn(name = "opened_by_employee_id", nullable = false)
    private EmployeeProfile openedBy;

    @ManyToOne
    @JoinColumn(name = "closed_by_employee_id")
    private EmployeeProfile closedBy;

    @Column(name = "opened_at", nullable = false)
    @Builder.Default
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
