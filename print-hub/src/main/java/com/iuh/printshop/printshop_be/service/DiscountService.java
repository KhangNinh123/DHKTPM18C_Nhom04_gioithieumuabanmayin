package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.discount.DiscountApplyRequest;
import com.iuh.printshop.printshop_be.dto.discount.DiscountApplyResponse;
import com.iuh.printshop.printshop_be.dto.discount.DiscountRequest;
import com.iuh.printshop.printshop_be.dto.discount.DiscountResponse;
import com.iuh.printshop.printshop_be.entity.Discount;
import com.iuh.printshop.printshop_be.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;

    public DiscountResponse createDiscount(DiscountRequest request) {
        // Check if code already exists
        if (discountRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Discount code already exists: " + request.getCode());
        }

        Discount discount = Discount.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscount(request.getMaxDiscount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return convertToDto(discountRepository.save(discount));
    }

    public List<DiscountResponse> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<DiscountResponse> getDiscountById(Integer id) {
        return discountRepository.findById(id).map(this::convertToDto);
    }

    public Optional<DiscountResponse> getDiscountByCode(String code) {
        return discountRepository.findByCode(code).map(this::convertToDto);
    }

    public Optional<DiscountResponse> updateDiscount(Integer id, DiscountRequest request) {
        return discountRepository.findById(id).map(discount -> {
            // Check if code is being changed and if new code already exists
            if (!discount.getCode().equals(request.getCode())) {
                if (discountRepository.findByCode(request.getCode()).isPresent()) {
                    throw new RuntimeException("Discount code already exists: " + request.getCode());
                }
            }

            discount.setCode(request.getCode());
            discount.setDescription(request.getDescription());
            discount.setDiscountType(request.getDiscountType());
            discount.setDiscountValue(request.getDiscountValue());
            discount.setMinOrderValue(request.getMinOrderValue());
            discount.setMaxDiscount(request.getMaxDiscount());
            discount.setUsageLimit(request.getUsageLimit());
            discount.setStartDate(request.getStartDate());
            discount.setEndDate(request.getEndDate());
            if (request.getIsActive() != null) {
                discount.setIsActive(request.getIsActive());
            }

            return convertToDto(discountRepository.save(discount));
        });
    }

    public boolean deleteDiscount(Integer id) {
        if (discountRepository.existsById(id)) {
            discountRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public DiscountApplyResponse applyDiscount(DiscountApplyRequest request, BigDecimal orderTotal) {
        Discount discount = discountRepository.findByCode(request.getCode())
                .orElseThrow(() -> new RuntimeException("Discount code not found: " + request.getCode()));

        // Validate discount
        validateDiscount(discount, orderTotal);

        // Calculate discount amount
        BigDecimal discountAmount = calculateDiscountAmount(discount, orderTotal);

        // Update used count
        discount.setUsedCount(discount.getUsedCount() + 1);
        discountRepository.save(discount);

        BigDecimal finalTotal = orderTotal.subtract(discountAmount);

        return DiscountApplyResponse.builder()
                .originalTotal(orderTotal)
                .discountAmount(discountAmount)
                .finalTotal(finalTotal)
                .discountCode(discount.getCode())
                .discountDescription(discount.getDescription())
                .build();
    }

    public boolean validateDiscount(String code, BigDecimal orderTotal) {
        Optional<Discount> discountOpt = discountRepository.findByCode(code);
        if (discountOpt.isEmpty()) {
            return false;
        }

        try {
            validateDiscount(discountOpt.get(), orderTotal);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void validateDiscount(Discount discount, BigDecimal orderTotal) {
        LocalDateTime now = LocalDateTime.now();

        // Check if discount is active
        if (!discount.getIsActive()) {
            throw new RuntimeException("Discount code is not active");
        }

        // Check date range
        if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
            throw new RuntimeException("Discount code is not valid at this time");
        }

        // Check usage limit
        if (discount.getUsageLimit() != null && discount.getUsedCount() >= discount.getUsageLimit()) {
            throw new RuntimeException("Discount code has reached its usage limit");
        }

        // Check minimum order value
        if (discount.getMinOrderValue() != null && orderTotal.compareTo(discount.getMinOrderValue()) < 0) {
            throw new RuntimeException("Order total must be at least " + discount.getMinOrderValue());
        }
    }

    private BigDecimal calculateDiscountAmount(Discount discount, BigDecimal orderTotal) {
        BigDecimal discountAmount;

        if (discount.getDiscountType() == Discount.DiscountType.PERCENTAGE) {
            // Calculate percentage discount
            discountAmount = orderTotal.multiply(discount.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // Fixed amount discount
            discountAmount = discount.getDiscountValue();
        }

        // Apply max discount limit if exists
        if (discount.getMaxDiscount() != null && discountAmount.compareTo(discount.getMaxDiscount()) > 0) {
            discountAmount = discount.getMaxDiscount();
        }

        // Ensure discount doesn't exceed order total
        if (discountAmount.compareTo(orderTotal) > 0) {
            discountAmount = orderTotal;
        }

        return discountAmount;
    }

    private DiscountResponse convertToDto(Discount discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .description(discount.getDescription())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .minOrderValue(discount.getMinOrderValue())
                .maxDiscount(discount.getMaxDiscount())
                .usageLimit(discount.getUsageLimit())
                .usedCount(discount.getUsedCount())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .isActive(discount.getIsActive())
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt())
                .build();
    }
}


