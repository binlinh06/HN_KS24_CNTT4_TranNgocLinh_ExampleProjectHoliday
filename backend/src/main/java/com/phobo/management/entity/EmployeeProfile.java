package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfile {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String position;

    @Builder.Default
    @Column(name = "hire_date", nullable = false)
    private LocalDateTime hireDate = LocalDateTime.now();

    @Column(name = "employee_code", unique = true)
    private String employeeCode;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Version
    private Integer version;
}
