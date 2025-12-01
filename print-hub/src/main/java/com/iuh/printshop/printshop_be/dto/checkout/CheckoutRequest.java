package com.iuh.printshop.printshop_be.dto.checkout;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String fullName;
    private String phone;
    private String shippingAddress;
    private String paymentMethod; // "VNPAY" or "COD"
}
