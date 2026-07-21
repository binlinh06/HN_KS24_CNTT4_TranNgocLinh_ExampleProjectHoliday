package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "shift_assignments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "work_date", "shift_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private WorkShift workShift;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @ManyToOne
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    private User assignedBy;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Builder.Default
    @Column(nullable = false)
    private String status = "SCHEDULED"; // SCHEDULED, COMPLETED, CANCELED

    @Version
    private Integer version;
}
