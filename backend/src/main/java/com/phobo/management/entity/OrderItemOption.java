package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemOption {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne
    @JoinColumn(name = "option_id", nullable = true)
    private ProductOption option;

    @Column(name = "option_group_name_snapshot", nullable = false)
    private String optionGroupNameSnapshot;

    @Column(name = "option_name_snapshot", nullable = false)
    private String optionNameSnapshot;

    @Column(name = "incremental_price_snapshot", nullable = false)
    private BigDecimal incrementalPriceSnapshot;
}
