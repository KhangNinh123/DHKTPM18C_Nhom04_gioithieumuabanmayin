package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.discount.DiscountApplyRequest;
import com.iuh.printshop.printshop_be.dto.discount.DiscountApplyResponse;
import com.iuh.printshop.printshop_be.dto.discount.DiscountRequest;
import com.iuh.printshop.printshop_be.dto.discount.DiscountResponse;
import com.iuh.printshop.printshop_be.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Tag(name = "Discounts", description = "Discount code management APIs")
public class DiscountController {
    private final DiscountService discountService;

    @PostMapping
    @Operation(summary = "Create discount code", description = "Create a new discount code")
    public ResponseEntity<?> createDiscount(@Valid @RequestBody DiscountRequest request) {
        try {
            DiscountResponse response = discountService.createDiscount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all discount codes", description = "Retrieve all discount codes")
    public ResponseEntity<List<DiscountResponse>> getAllDiscounts() {
        List<DiscountResponse> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discount by ID", description = "Retrieve a discount code by its ID")
    public ResponseEntity<DiscountResponse> getDiscountById(@PathVariable Integer id) {
        return discountService.getDiscountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get discount by code", description = "Retrieve a discount code by its code string")
    public ResponseEntity<DiscountResponse> getDiscountByCode(@PathVariable String code) {
        return discountService.getDiscountByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update discount code", description = "Update an existing discount code")
    public ResponseEntity<?> updateDiscount(
            @PathVariable Integer id,
            @Valid @RequestBody DiscountRequest request) {
        try {
            return discountService.updateDiscount(id, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete discount code", description = "Delete a discount code by its ID")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Integer id) {
        if (discountService.deleteDiscount(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply discount code", description = "Calculate discount for an order using a discount code")
    public ResponseEntity<?> applyDiscount(
            @RequestParam BigDecimal orderTotal,
            @Valid @RequestBody DiscountApplyRequest request) {
        try {
            DiscountApplyResponse response = discountService.applyDiscount(request, orderTotal);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate discount code", description = "Check if a discount code is valid (without calculating)")
    public ResponseEntity<Boolean> validateDiscount(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal) {
        boolean isValid = discountService.validateDiscount(code, orderTotal);
        return ResponseEntity.ok(isValid);
    }
}


