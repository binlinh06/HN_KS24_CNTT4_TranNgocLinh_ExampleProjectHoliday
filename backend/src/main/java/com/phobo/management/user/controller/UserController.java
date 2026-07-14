package com.phobo.management.user.controller;

import com.phobo.management.user.service.UserService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for User.
 * Relevant Use Cases: UC-21
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-21 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(userService.getInfo(), "Skeleton active"));
    }
}
