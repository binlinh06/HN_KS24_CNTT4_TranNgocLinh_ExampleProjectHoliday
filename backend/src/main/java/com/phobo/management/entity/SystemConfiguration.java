package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfiguration {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "config_key", unique = true, nullable = false)
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_group", nullable = false)
    private String configGroup;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "value_type", nullable = false)
    private String valueType = "STRING";

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "updated_by", nullable = false, columnDefinition = "CHAR(36)")
    private String updatedBy;

    @Version
    private Integer version;
}
