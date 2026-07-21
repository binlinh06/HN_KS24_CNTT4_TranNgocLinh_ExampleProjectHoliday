package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserRole.UserRoleId.class)
public class UserRole {
    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    @Id
    @Column(name = "role_id", columnDefinition = "CHAR(36)")
    private String roleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleId implements Serializable {
        private String userId;
        private String roleId;
    }
}
