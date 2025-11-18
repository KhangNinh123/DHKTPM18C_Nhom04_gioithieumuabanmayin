package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.auth.AuthResponse;
import com.iuh.printshop.printshop_be.dto.auth.LoginRequest;
import com.iuh.printshop.printshop_be.dto.auth.RegisterRequest;
import com.iuh.printshop.printshop_be.dto.auth.RegisterResponse;
import com.iuh.printshop.printshop_be.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account with ROLE_CUSTOMER")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            RegisterResponse registerResponse = new RegisterResponse(
                    response.getId(),
                    response.getEmail(),
                    response.getFullName(),
                    response.getPhone(),
                    response.getDefaultAddress(),
                    response.getRoles()
            );
            return ResponseEntity.ok(registerResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Generate new JWT token for authenticated user")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String email) {
        try {
            AuthResponse response = authService.refreshToken(email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with verification token")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            boolean verified = authService.verifyEmail(token);
            if (verified) {
                return ResponseEntity.ok("Email đã được xác nhận thành công!");
            }
            return ResponseEntity.badRequest().body("Xác nhận email thất bại");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/verify-email-otp")
    @Operation(summary = "Verify email with OTP", description = "Verify user email with OTP code and activate account")
    public ResponseEntity<String> verifyEmailWithOtp(@RequestParam String email, @RequestParam String otpCode) {
        try {
            boolean verified = authService.verifyEmailWithOtp(email, otpCode);
            if (verified) {
                return ResponseEntity.ok("Tài khoản đã được kích hoạt thành công! Bạn có thể đăng nhập ngay bây giờ.");
            }
            return ResponseEntity.badRequest().body("Xác thực OTP thất bại");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resend verification email with new OTP")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email) {
        try {
            authService.resendVerificationEmail(email);
            return ResponseEntity.ok("Email xác nhận đã được gửi lại!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
