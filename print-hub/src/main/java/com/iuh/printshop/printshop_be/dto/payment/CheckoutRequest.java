package com.iuh.printshop.printshop_be.dto.payment;

import com.iuh.printshop.printshop_be.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotNull
    private PaymentMethod method; // VNPAY / MOMO / STRIPE / COD
    private String successUrl;    // Optional redirect
    private String cancelUrl;     // Optional
}
