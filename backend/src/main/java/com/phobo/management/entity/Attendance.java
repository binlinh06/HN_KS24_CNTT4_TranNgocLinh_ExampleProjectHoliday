package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employee;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    private ShiftAssignment shiftAssignment;

    @Column(name = "work_date")
    private java.time.LocalDate workDate;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(name = "status")
    private String status;

    @Column(name = "check_in_source")
    private String checkInSource;

    @Column(name = "check_out_source")
    private String checkOutSource;

    @Column(name = "adjust_reason", columnDefinition = "TEXT")
    private String adjustReason;

    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Version
    private Integer version;
}
