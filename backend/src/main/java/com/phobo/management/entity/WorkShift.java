package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "work_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkShift {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "shift_code", unique = true, nullable = false)
    private String shiftCode;

    @Column(name = "shift_name", nullable = false)
    private String shiftName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Builder.Default
    @Column(name = "crosses_midnight", nullable = false)
    private Boolean crossesMidnight = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Version
    private Integer version;
}
