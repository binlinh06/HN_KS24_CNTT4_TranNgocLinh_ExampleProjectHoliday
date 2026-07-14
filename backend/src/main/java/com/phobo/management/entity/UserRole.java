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
@IdClass(UserRole.UserRoleId.class)
public class UserRole {
    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    @Id
    @Column(name = "role_id", columnDefinition = "CHAR(36)")
    private String roleId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleId implements Serializable {
        private String userId;
        private String roleId;
    }
}
