package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.auth.AuthResponse;
import com.iuh.printshop.printshop_be.dto.auth.LoginRequest;
import com.iuh.printshop.printshop_be.dto.auth.RegisterRequest;
import com.iuh.printshop.printshop_be.entity.OtpVerification;
import com.iuh.printshop.printshop_be.entity.Role;
import com.iuh.printshop.printshop_be.entity.User;
import com.iuh.printshop.printshop_be.repository.OtpVerificationRepository;
import com.iuh.printshop.printshop_be.repository.RoleRepository;
import com.iuh.printshop.printshop_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    
    private final Random random = new Random();
    private static final int OTP_EXPIRATION_MINUTES = 10;
    
    private String generateOtpCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    private void saveOtpVerification(String email, String otpCode) {
        // Invalidate all previous OTPs for this email
        otpVerificationRepository.invalidateAllOtpsForEmail(email);
        
        // Create new OTP verification
        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otpCode(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .isUsed(false)
                .build();
        
        otpVerificationRepository.save(otpVerification);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDefaultAddress(request.getDefaultAddress());
        user.setIsActive(false); // Tài khoản chưa kích hoạt, chờ nhập mã OTP

        // Assign default role (ROLE_CUSTOMER)
        Set<Role> roles = new HashSet<>();
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));
        roles.add(customerRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Generate OTP, lưu vào database và gửi email
        String otpCode = generateOtpCode();
        saveOtpVerification(request.getEmail(), otpCode);
        emailService.sendVerificationEmail(request.getEmail(), otpCode);

        // Return response without JWT token (user needs to verify OTP first)
        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getPhone(),
                savedUser.getDefaultAddress(),
                savedUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmailWithRoles(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if account is activated
            if (!user.getIsActive()) {
                throw new RuntimeException("Tài khoản chưa được kích hoạt. Vui lòng xác thực email trước.");
            }

            // Generate JWT token
            String token = jwtService.generateToken(userDetails);

            return new AuthResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhone(),
                    user.getDefaultAddress(),
                    user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet())
            );
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Login error: " + e.getMessage());
            throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getDefaultAddress(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    @Transactional
    public boolean verifyEmail(String token) {
        // Method này giữ lại để tương thích ngược, nhưng không còn sử dụng
        return true;
    }
    
    @Transactional
    public boolean verifyEmailWithOtp(String email, String otpCode) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        // Check if account is already activated
        if (user.getIsActive()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt");
        }

        // Validate OTP format
        if (otpCode == null || otpCode.length() != 6 || !otpCode.matches("\\d{6}")) {
            throw new RuntimeException("Mã OTP không hợp lệ. Mã OTP phải có 6 chữ số.");
        }

        // Find OTP verification
        OtpVerification otpVerification = otpVerificationRepository
                .findByEmailAndOtpCodeAndIsUsedFalse(email, otpCode)
                .orElseThrow(() -> new RuntimeException("Mã OTP không đúng hoặc đã được sử dụng"));

        // Check if OTP is expired
        if (otpVerification.isExpired()) {
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        // Check if OTP is already used
        if (otpVerification.getIsUsed()) {
            throw new RuntimeException("Mã OTP đã được sử dụng");
        }

        // Mark OTP as used
        otpVerification.setIsUsed(true);
        otpVerificationRepository.save(otpVerification);

        // Activate user account
        user.setIsActive(true);
        userRepository.save(user);

        return true;
    }
    
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        if (user.getIsActive()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt");
        }

        // Generate new OTP, save to database and send email
        String otpCode = generateOtpCode();
        saveOtpVerification(email, otpCode);
        emailService.sendVerificationEmail(email, otpCode);
    }
    
}
