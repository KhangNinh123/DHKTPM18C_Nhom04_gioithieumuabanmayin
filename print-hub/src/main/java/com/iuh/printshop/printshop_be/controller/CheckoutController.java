package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.checkout.CheckoutRequest;
import com.iuh.printshop.printshop_be.dto.checkout.CheckoutResponse;
import com.iuh.printshop.printshop_be.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> checkout(
            @PathVariable Integer userId,
            @RequestBody CheckoutRequest request
    ) {
        CheckoutResponse res = checkoutService.createOrder(userId, request);
        return ResponseEntity.ok(res);
    }
}
