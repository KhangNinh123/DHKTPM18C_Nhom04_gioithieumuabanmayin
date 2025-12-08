package com.iuh.printshop.printshop_be.dto.checkout;

import lombok.Data;

@Data
public class CheckoutResponse {
    private Long orderId;
    private String orderCode;

    // null nếu COD
    private String redirectUrl;

    public CheckoutResponse(Long id, String code, Object o) {
    }
}
