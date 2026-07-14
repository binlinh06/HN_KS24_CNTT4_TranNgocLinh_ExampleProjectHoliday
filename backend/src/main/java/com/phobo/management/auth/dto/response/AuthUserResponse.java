package com.phobo.management.auth.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AuthUserResponse {
    private String id;
    private String username;
    private String email;
    private String phone;
    private List<String> roles;
    private String fullName;
}
