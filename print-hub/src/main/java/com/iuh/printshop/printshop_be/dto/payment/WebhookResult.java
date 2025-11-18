package com.iuh.printshop.printshop_be.dto.payment;

import lombok.Data;

@Data
public class WebhookResult {
    private Long paymentId;
    private String newStatus;
    private String message;
}
