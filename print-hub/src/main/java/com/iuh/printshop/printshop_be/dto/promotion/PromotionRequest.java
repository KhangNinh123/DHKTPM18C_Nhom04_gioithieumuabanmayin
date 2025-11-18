package com.iuh.printshop.printshop_be.dto.promotion;

import com.iuh.printshop.printshop_be.entity.Promotion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {
    @NotBlank(message = "Promotion name cannot be empty")
    private String name;

    private String description;

    @NotNull(message = "Discount type cannot be null")
    private Promotion.DiscountType discountType;

    @NotNull(message = "Discount value cannot be null")
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;

    private List<Integer> applicableCategoryIds;

    private List<Integer> applicableProductIds;

    private BigDecimal minOrderValue;

    private BigDecimal maxDiscount;

    @NotNull(message = "Start date cannot be null")
    private LocalDateTime startDate;

    @NotNull(message = "End date cannot be null")
    private LocalDateTime endDate;

    private Boolean isActive;
}


