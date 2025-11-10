package com.iuh.printshop.printshop_be.dto.payment;

import lombok.Data;

@Data
public class WebhookResult {
    public enum Status { PAID, FAILED, REFUNDED }
    private String providerTxId;
    private Status status;
}
