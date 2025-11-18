package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.review.ReviewRequest;
import com.iuh.printshop.printshop_be.dto.review.ReviewResponse;
import com.iuh.printshop.printshop_be.entity.Product;
import com.iuh.printshop.printshop_be.entity.Review;
import com.iuh.printshop.printshop_be.entity.User;
import com.iuh.printshop.printshop_be.repository.ProductRepository;
import com.iuh.printshop.printshop_be.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    /**
     * Lấy tất cả reviews của một sản phẩm
     */
    public List<ReviewResponse> getReviewsByProductId(Integer productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo review mới
     */
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Lấy user hiện tại
        User user = userService.getCurrentUser();

        // Kiểm tra user đã review sản phẩm này chưa
        if (reviewRepository.existsByProductIdAndUserId(product.getId(), user.getId())) {
            throw new RuntimeException("You have already reviewed this product");
        }

        // Tạo review
        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        review = reviewRepository.save(review);
        return mapToResponse(review);
    }

    /**
     * Cập nhật review
     */
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Lấy user hiện tại
        User user = userService.getCurrentUser();

        // Kiểm tra quyền: chỉ user tạo review mới được update
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own review");
        }

        // Cập nhật
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());

        review = reviewRepository.save(review);
        return mapToResponse(review);
    }

    /**
     * Xóa review
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Lấy user hiện tại
        User user = userService.getCurrentUser();

        // Kiểm tra quyền: user tạo review hoặc admin mới được xóa
        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You can only delete your own review");
        }

        reviewRepository.delete(review);
    }

    /**
     * Lấy reviews của user hiện tại
     */
    public List<ReviewResponse> getMyReviews() {
        User user = userService.getCurrentUser();

        List<Review> reviews = reviewRepository.findByUserId(user.getId());
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Review entity sang ReviewResponse
     */
    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .userEmail(review.getUser().getEmail())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

