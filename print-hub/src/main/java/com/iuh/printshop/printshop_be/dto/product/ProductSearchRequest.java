package com.iuh.printshop.printshop_be.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    private String keyword;
    private Integer categoryId;
    private Integer brandId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy; // price_asc, price_desc, name_asc, name_desc, newest
    private Integer page = 0;
    private Integer size = 20;
}

