package com.iuh.printshop.printshop_be.dto.discount;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountApplyRequest {
    @NotBlank(message = "Discount code cannot be empty")
    private String code;
}


