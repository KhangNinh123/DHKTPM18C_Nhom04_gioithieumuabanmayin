package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.cart.AddToCartRequest;
import com.iuh.printshop.printshop_be.dto.cart.CartResponse;
import com.iuh.printshop.printshop_be.dto.cart.UpdateCartItemRequest;
import com.iuh.printshop.printshop_be.repository.UserRepository;
import com.iuh.printshop.printshop_be.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private Integer getUserId(Authentication authentication) {
        String email = authentication.getName();  // lấy email từ token
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping("/my-cart")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        Integer userId = getUserId(authentication);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest req) {

        Integer userId = getUserId(authentication);
        return ResponseEntity.ok(cartService.addItem(userId, req));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            Authentication authentication,
            @PathVariable Integer itemId,
            @Valid @RequestBody UpdateCartItemRequest req) {

        Integer userId = getUserId(authentication);
        return ResponseEntity.ok(cartService.updateItem(userId, itemId, req));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            Authentication authentication,
            @PathVariable Integer itemId) {

        Integer userId = getUserId(authentication);
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clear(Authentication authentication) {
        Integer userId = getUserId(authentication);
        return ResponseEntity.ok(cartService.clear(userId));
    }
}
