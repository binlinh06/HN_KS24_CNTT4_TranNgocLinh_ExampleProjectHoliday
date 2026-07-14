package com.phobo.management.entity;

import com.phobo.management.common.enums.RoleCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleCode code;

    @Column(nullable = false)
    private String name;

    private String description;
}
