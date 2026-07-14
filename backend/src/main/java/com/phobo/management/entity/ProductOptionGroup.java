package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "product_option_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProductOptionGroup.ProductOptionGroupId.class)
public class ProductOptionGroup {
    @Id
    @Column(name = "product_id", columnDefinition = "CHAR(36)")
    private String productId;

    @Id
    @Column(name = "group_id", columnDefinition = "CHAR(36)")
    private String groupId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductOptionGroupId implements Serializable {
        private String productId;
        private String groupId;
    }
}
