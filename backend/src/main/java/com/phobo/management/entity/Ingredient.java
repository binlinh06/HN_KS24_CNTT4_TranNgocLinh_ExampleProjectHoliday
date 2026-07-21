package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "ingredient_code", unique = true)
    private String ingredientCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unit;

    @Column(name = "min_threshold", nullable = false)
    private BigDecimal minThreshold;

    @Builder.Default
    @Column(name = "current_stock", nullable = false)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Version
    private Integer version;
}
