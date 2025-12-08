package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.checkout.CheckoutRequest;
import com.iuh.printshop.printshop_be.dto.checkout.CheckoutResponse;
import com.iuh.printshop.printshop_be.repository.UserRepository;
import com.iuh.printshop.printshop_be.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final UserRepository userRepository;

    private Integer getUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @PostMapping
    public ResponseEntity<?> checkout(
            Authentication authentication,
            @RequestBody CheckoutRequest request
    ) {
        Integer userId = getUserId(authentication);
        CheckoutResponse res = checkoutService.createOrder(userId, request);
        return ResponseEntity.ok(res);
    }
}
