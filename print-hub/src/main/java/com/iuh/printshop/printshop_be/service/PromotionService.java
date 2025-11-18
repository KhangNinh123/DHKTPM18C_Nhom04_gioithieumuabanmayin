package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.promotion.PromotionCalculateResponse;
import com.iuh.printshop.printshop_be.dto.promotion.PromotionRequest;
import com.iuh.printshop.printshop_be.dto.promotion.PromotionResponse;
import com.iuh.printshop.printshop_be.entity.Category;
import com.iuh.printshop.printshop_be.entity.Product;
import com.iuh.printshop.printshop_be.entity.Promotion;
import com.iuh.printshop.printshop_be.repository.CategoryRepository;
import com.iuh.printshop.printshop_be.repository.ProductRepository;
import com.iuh.printshop.printshop_be.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public PromotionResponse createPromotion(PromotionRequest request) {
        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscount(request.getMaxDiscount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        // Set applicable categories
        if (request.getApplicableCategoryIds() != null && !request.getApplicableCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Integer categoryId : request.getApplicableCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
                categories.add(category);
            }
            promotion.setApplicableCategories(categories);
        }

        // Set applicable products
        if (request.getApplicableProductIds() != null && !request.getApplicableProductIds().isEmpty()) {
            Set<Product> products = new HashSet<>();
            for (Integer productId : request.getApplicableProductIds()) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
                products.add(product);
            }
            promotion.setApplicableProducts(products);
        }

        return convertToDto(promotionRepository.save(promotion));
    }

    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PromotionResponse> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findActivePromotions(now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<PromotionResponse> getPromotionById(Integer id) {
        return promotionRepository.findById(id).map(this::convertToDto);
    }

    public Optional<PromotionResponse> updatePromotion(Integer id, PromotionRequest request) {
        return promotionRepository.findById(id).map(promotion -> {
            promotion.setName(request.getName());
            promotion.setDescription(request.getDescription());
            promotion.setDiscountType(request.getDiscountType());
            promotion.setDiscountValue(request.getDiscountValue());
            promotion.setMinOrderValue(request.getMinOrderValue());
            promotion.setMaxDiscount(request.getMaxDiscount());
            promotion.setStartDate(request.getStartDate());
            promotion.setEndDate(request.getEndDate());
            if (request.getIsActive() != null) {
                promotion.setIsActive(request.getIsActive());
            }

            // Update applicable categories
            if (request.getApplicableCategoryIds() != null) {
                Set<Category> categories = new HashSet<>();
                for (Integer categoryId : request.getApplicableCategoryIds()) {
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
                    categories.add(category);
                }
                promotion.setApplicableCategories(categories);
            }

            // Update applicable products
            if (request.getApplicableProductIds() != null) {
                Set<Product> products = new HashSet<>();
                for (Integer productId : request.getApplicableProductIds()) {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
                    products.add(product);
                }
                promotion.setApplicableProducts(products);
            }

            return convertToDto(promotionRepository.save(promotion));
        });
    }

    public boolean deletePromotion(Integer id) {
        if (promotionRepository.existsById(id)) {
            promotionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public PromotionCalculateResponse calculatePromotionDiscount(
            BigDecimal orderTotal,
            List<Integer> productIds,
            List<Integer> categoryIds) {

        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(now);

        // Find applicable promotions
        Promotion applicablePromotion = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (Promotion promotion : activePromotions) {
            if (!promotion.getIsActive()) {
                continue;
            }

            // Check if promotion applies to this order
            boolean isApplicable = false;

            // Check category match
            if (categoryIds != null && !categoryIds.isEmpty() &&
                promotion.getApplicableCategories() != null && !promotion.getApplicableCategories().isEmpty()) {
                Set<Integer> promotionCategoryIds = promotion.getApplicableCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet());
                if (categoryIds.stream().anyMatch(promotionCategoryIds::contains)) {
                    isApplicable = true;
                }
            }

            // Check product match
            if (!isApplicable && productIds != null && !productIds.isEmpty() &&
                promotion.getApplicableProducts() != null && !promotion.getApplicableProducts().isEmpty()) {
                Set<Integer> promotionProductIds = promotion.getApplicableProducts().stream()
                        .map(Product::getId)
                        .collect(Collectors.toSet());
                if (productIds.stream().anyMatch(promotionProductIds::contains)) {
                    isApplicable = true;
                }
            }

            // If no specific categories/products, promotion applies to all
            if (!isApplicable &&
                (promotion.getApplicableCategories() == null || promotion.getApplicableCategories().isEmpty()) &&
                (promotion.getApplicableProducts() == null || promotion.getApplicableProducts().isEmpty())) {
                isApplicable = true;
            }

            if (isApplicable) {
                // Check minimum order value
                if (promotion.getMinOrderValue() != null &&
                    orderTotal.compareTo(promotion.getMinOrderValue()) < 0) {
                    continue;
                }

                // Calculate discount amount
                BigDecimal discountAmount = calculateDiscountAmount(promotion, orderTotal);

                // Find the promotion with maximum discount
                if (discountAmount.compareTo(maxDiscount) > 0) {
                    maxDiscount = discountAmount;
                    applicablePromotion = promotion;
                }
            }
        }

        if (applicablePromotion == null) {
            // No applicable promotion found
            return PromotionCalculateResponse.builder()
                    .originalTotal(orderTotal)
                    .discountAmount(BigDecimal.ZERO)
                    .finalTotal(orderTotal)
                    .discountCode(null)
                    .discountDescription(null)
                    .build();
        }

        BigDecimal finalTotal = orderTotal.subtract(maxDiscount);

        return PromotionCalculateResponse.builder()
                .originalTotal(orderTotal)
                .discountAmount(maxDiscount)
                .finalTotal(finalTotal)
                .discountCode(applicablePromotion.getName())
                .discountDescription(applicablePromotion.getDescription())
                .build();
    }

    private BigDecimal calculateDiscountAmount(Promotion promotion, BigDecimal orderTotal) {
        BigDecimal discountAmount;

        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
            // Calculate percentage discount
            discountAmount = orderTotal.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // Fixed amount discount
            discountAmount = promotion.getDiscountValue();
        }

        // Apply max discount limit if exists
        if (promotion.getMaxDiscount() != null && discountAmount.compareTo(promotion.getMaxDiscount()) > 0) {
            discountAmount = promotion.getMaxDiscount();
        }

        // Ensure discount doesn't exceed order total
        if (discountAmount.compareTo(orderTotal) > 0) {
            discountAmount = orderTotal;
        }

        return discountAmount;
    }

    private PromotionResponse convertToDto(Promotion promotion) {
        List<Integer> categoryIds = null;
        List<String> categoryNames = null;
        if (promotion.getApplicableCategories() != null && !promotion.getApplicableCategories().isEmpty()) {
            categoryIds = promotion.getApplicableCategories().stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
            categoryNames = promotion.getApplicableCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
        }

        List<Integer> productIds = null;
        List<String> productNames = null;
        if (promotion.getApplicableProducts() != null && !promotion.getApplicableProducts().isEmpty()) {
            productIds = promotion.getApplicableProducts().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            productNames = promotion.getApplicableProducts().stream()
                    .map(Product::getName)
                    .collect(Collectors.toList());
        }

        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .applicableCategoryIds(categoryIds)
                .applicableCategoryNames(categoryNames)
                .applicableProductIds(productIds)
                .applicableProductNames(productNames)
                .minOrderValue(promotion.getMinOrderValue())
                .maxDiscount(promotion.getMaxDiscount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .build();
    }
}


