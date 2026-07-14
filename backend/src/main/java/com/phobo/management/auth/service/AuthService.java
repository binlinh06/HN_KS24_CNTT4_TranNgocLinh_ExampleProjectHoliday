package com.phobo.management.auth.service;

import com.phobo.management.auth.dto.request.LoginRequest;
import com.phobo.management.auth.dto.request.RegisterRequest;
import com.phobo.management.auth.dto.response.AuthUserResponse;
import com.phobo.management.auth.dto.response.LoginResponse;
import com.phobo.management.entity.*;
import com.phobo.management.common.enums.*;
import com.phobo.management.repository.*;
import com.phobo.management.security.JwtService;
import com.phobo.management.exception.BadRequestException;
import com.phobo.management.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final CartRepository cartRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       CustomerProfileRepository customerProfileRepository, EmployeeProfileRepository employeeProfileRepository,
                       CartRepository cartRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.cartRepository = cartRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Tài khoản không chính xác hoặc không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Email hoặc mật khẩu không chính xác");
        }

        if (user.getStatus() == UserStatus.LOCKED || user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("Tài khoản đã bị khóa hoặc ngừng hoạt động");
        }

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getCode().name())
                .collect(Collectors.toList());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail(), roles);

        // Store refresh token
        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(rt);

        String fullName = "Người dùng";
        if (roles.contains("CUSTOMER")) {
            fullName = customerProfileRepository.findByUserId(user.getId())
                    .map(CustomerProfile::getFullName).orElse(fullName);
        } else {
            fullName = employeeProfileRepository.findByUserId(user.getId())
                    .map(EmployeeProfile::getFullName).orElse(fullName);
        }

        AuthUserResponse userResponse = AuthUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .fullName(fullName)
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent() ||
            userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BadRequestException("Email hoặc số điện thoại đã tồn tại trên hệ thống");
        }

        Role customerRole = roleRepository.findByCode(RoleCode.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Quyền CUSTOMER không tồn tại"));

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getEmail())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(customerRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        CustomerProfile profile = CustomerProfile.builder()
                .id(UUID.randomUUID().toString())
                .user(savedUser)
                .fullName(request.getFullName())
                .loyaltyPoints(0)
                .build();

        CustomerProfile savedProfile = customerProfileRepository.save(profile);

        // Create initial cart
        Cart cart = Cart.builder()
                .id(UUID.randomUUID().toString())
                .customer(savedProfile)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        cartRepository.save(cart);

        return AuthUserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .roles(List.of("CUSTOMER"))
                .fullName(savedProfile.getFullName())
                .build();
    }

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new BadRequestException("Refresh token không hợp lệ");
        }

        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Refresh token không tồn tại hoặc đã hết hạn"));

        if (rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(rt);
            throw new BadRequestException("Refresh token đã hết hạn");
        }

        User user = rt.getUser();
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getCode().name())
                .collect(Collectors.toList());

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
        String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail(), roles);

        // Update token
        rt.setToken(newRefreshToken);
        rt.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(rt);

        String fullName = "Người dùng";
        if (roles.contains("CUSTOMER")) {
            fullName = customerProfileRepository.findByUserId(user.getId())
                    .map(CustomerProfile::getFullName).orElse(fullName);
        } else {
            fullName = employeeProfileRepository.findByUserId(user.getId())
                    .map(EmployeeProfile::getFullName).orElse(fullName);
        }

        AuthUserResponse userResponse = AuthUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .fullName(fullName)
                .build();

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userResponse)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }
    }

    public AuthUserResponse getMe(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getCode().name())
                .collect(Collectors.toList());

        String fullName = "Người dùng";
        if (roles.contains("CUSTOMER")) {
            fullName = customerProfileRepository.findByUserId(user.getId())
                    .map(CustomerProfile::getFullName).orElse(fullName);
        } else {
            fullName = employeeProfileRepository.findByUserId(user.getId())
                    .map(EmployeeProfile::getFullName).orElse(fullName);
        }

        return AuthUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .fullName(fullName)
                .build();
    }
}
