package com.iuh.printshop.printshop_be.dto.discount;

import com.iuh.printshop.printshop_be.entity.Discount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountRequest {
    @NotBlank(message = "Discount code cannot be empty")
    private String code;

    private String description;

    @NotNull(message = "Discount type cannot be null")
    private Discount.DiscountType discountType;

    @NotNull(message = "Discount value cannot be null")
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;

    private BigDecimal minOrderValue;

    private BigDecimal maxDiscount;

    private Integer usageLimit;

    @NotNull(message = "Start date cannot be null")
    private LocalDateTime startDate;

    @NotNull(message = "End date cannot be null")
    private LocalDateTime endDate;

    private Boolean isActive;
}


