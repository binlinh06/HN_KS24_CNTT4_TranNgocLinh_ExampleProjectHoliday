package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "cart_item_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CartItemOption.CartItemOptionId.class)
public class CartItemOption {
    @Id
    @Column(name = "cart_item_id", columnDefinition = "CHAR(36)")
    private String cartItemId;

    @Id
    @Column(name = "option_id", columnDefinition = "CHAR(36)")
    private String optionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemOptionId implements Serializable {
        private String cartItemId;
        private String optionId;
    }
}
