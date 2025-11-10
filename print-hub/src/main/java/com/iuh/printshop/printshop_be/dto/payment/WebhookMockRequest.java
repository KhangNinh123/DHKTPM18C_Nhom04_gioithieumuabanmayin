package com.iuh.printshop.printshop_be.dto.payment;

import lombok.Data;

@Data
public class WebhookMockRequest {
    private String txId;   // provider transaction id
    private String status; // PAID | FAILED | REFUNDED
}
