package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "order_item_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrderItemOption.OrderItemOptionId.class)
public class OrderItemOption {
    @Id
    @Column(name = "order_item_id", columnDefinition = "CHAR(36)")
    private String orderItemId;

    @Id
    @Column(name = "option_id", columnDefinition = "CHAR(36)")
    private String optionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemOptionId implements Serializable {
        private String orderItemId;
        private String optionId;
    }
}
