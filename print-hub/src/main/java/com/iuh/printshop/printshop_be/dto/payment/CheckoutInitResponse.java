package com.iuh.printshop.printshop_be.dto.payment;

import com.iuh.printshop.printshop_be.entity.PaymentMethod;
import com.iuh.printshop.printshop_be.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CheckoutInitResponse {
    private Integer orderId;
    private Integer paymentId;
    private PaymentMethod method;
    private PaymentStatus status;
    private String paymentUrlOrClientSecret;
    private BigDecimal amount;
}
