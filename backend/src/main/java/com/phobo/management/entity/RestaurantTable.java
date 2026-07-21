package com.phobo.management.entity;

import com.phobo.management.common.enums.RestaurantTableStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "table_number", unique = true, nullable = false)
    private String tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RestaurantTableStatus status = RestaurantTableStatus.AVAILABLE;

    @Column(name = "status_updated_at")
    private java.time.LocalDateTime statusUpdatedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
