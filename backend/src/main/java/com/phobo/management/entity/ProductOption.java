package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOption {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private OptionGroup group;

    @Column(name = "option_name", nullable = false)
    private String optionName;

    @Column(name = "incremental_price", nullable = false)
    private BigDecimal incrementalPrice;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
}
