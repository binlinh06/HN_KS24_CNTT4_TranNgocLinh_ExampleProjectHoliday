package com.phobo.management.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private String id;
    private String userId;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String employeeCode;
    private String position;
    private Boolean isActive;
    private LocalDateTime hireDate;
    private LocalDateTime createdAt;
}
