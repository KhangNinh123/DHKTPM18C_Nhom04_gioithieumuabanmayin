package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.promotion.PromotionCalculateResponse;
import com.iuh.printshop.printshop_be.dto.promotion.PromotionRequest;
import com.iuh.printshop.printshop_be.dto.promotion.PromotionResponse;
import com.iuh.printshop.printshop_be.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Promotion management APIs")
public class PromotionController {
    private final PromotionService promotionService;

    @PostMapping
    @Operation(summary = "Create promotion", description = "Create a new promotion")
    public ResponseEntity<?> createPromotion(@Valid @RequestBody PromotionRequest request) {
        try {
            PromotionResponse response = promotionService.createPromotion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all promotions", description = "Retrieve all promotions")
    public ResponseEntity<List<PromotionResponse>> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active promotions", description = "Retrieve only active promotions within valid date range")
    public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get promotion by ID", description = "Retrieve a promotion by its ID")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable Integer id) {
        return promotionService.getPromotionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update promotion", description = "Update an existing promotion")
    public ResponseEntity<?> updatePromotion(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionRequest request) {
        try {
            return promotionService.updatePromotion(id, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete promotion", description = "Delete a promotion by its ID")
    public ResponseEntity<Void> deletePromotion(@PathVariable Integer id) {
        if (promotionService.deletePromotion(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate promotion discount", description = "Calculate discount from applicable promotions for an order")
    public ResponseEntity<PromotionCalculateResponse> calculatePromotion(
            @RequestParam BigDecimal orderTotal,
            @RequestParam(required = false) String productIds,
            @RequestParam(required = false) String categoryIds) {
        
        List<Integer> productIdList = null;
        if (productIds != null && !productIds.isEmpty()) {
            productIdList = Arrays.stream(productIds.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        List<Integer> categoryIdList = null;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categoryIdList = Arrays.stream(categoryIds.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        PromotionCalculateResponse response = promotionService.calculatePromotionDiscount(
                orderTotal, productIdList, categoryIdList);
        return ResponseEntity.ok(response);
    }
}


