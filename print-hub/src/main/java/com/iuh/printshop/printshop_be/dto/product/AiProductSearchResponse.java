package com.iuh.printshop.printshop_be.dto.product;

import com.iuh.printshop.printshop_be.dto.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProductSearchResponse {
    private String reply;
    private List<Integer> recommendedIds;
    private List<ProductResponse> products;
}
