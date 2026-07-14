package com.phobo.management.auth.controller;

import com.phobo.management.auth.dto.request.LoginRequest;
import com.phobo.management.auth.dto.request.RegisterRequest;
import com.phobo.management.auth.dto.response.AuthUserResponse;
import com.phobo.management.auth.dto.response.LoginResponse;
import com.phobo.management.auth.service.AuthService;
import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.security.CustomUserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(loginRequest);
        
        // Save Refresh Token in HttpOnly Cookie
        Cookie refreshCookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // set to true in production
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Đăng nhập thành công"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthUserResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        AuthUserResponse registered = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(registered, "Đăng ký tài khoản thành công"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        LoginResponse loginResponse = authService.refresh(refreshToken);

        // Update cookie
        Cookie refreshCookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Cấp mới access token thành công"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        authService.logout(refreshToken);

        // Clear cookie
        Cookie clearCookie = new Cookie("refresh_token", null);
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(false);
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);

        return ResponseEntity.ok(ApiResponse.success(null, "Đăng xuất thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getMe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.<AuthUserResponse>builder()
                    .success(false)
                    .message("Yêu cầu xác thực tài khoản")
                    .build());
        }
        AuthUserResponse me = authService.getMe(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(me, "Lấy thông tin cá nhân thành công"));
    }
}
