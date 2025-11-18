package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.review.ReviewRequest;
import com.iuh.printshop.printshop_be.dto.review.ReviewResponse;
import com.iuh.printshop.printshop_be.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews by product ID", description = "Get all reviews for a specific product")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProductId(@PathVariable Integer productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    @Operation(summary = "Create review", description = "Create a new review (requires authentication)")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update review", description = "Update an existing review (only by review owner)")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse review = reviewService.updateReview(id, request);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review", description = "Delete a review (only by review owner or admin)")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get my reviews", description = "Get all reviews created by current user")
    public ResponseEntity<List<ReviewResponse>> getMyReviews() {
        List<ReviewResponse> reviews = reviewService.getMyReviews();
        return ResponseEntity.ok(reviews);
    }
}

