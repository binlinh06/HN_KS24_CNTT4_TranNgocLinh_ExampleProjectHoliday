package com.phobo.management.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String status;
    private List<String> roles;
    private LocalDateTime createdAt;
}
