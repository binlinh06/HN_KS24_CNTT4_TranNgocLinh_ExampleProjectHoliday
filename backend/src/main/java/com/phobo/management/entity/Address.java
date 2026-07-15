package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "address_book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "address_detail", nullable = false, columnDefinition = "TEXT")
    private String addressDetail;

    @Column(name = "address_label")
    private String addressLabel;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt = java.time.LocalDateTime.now();
}
