package com.iuh.printshop.printshop_be.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitPaymentResult {
    private String providerTxId;
    private String checkoutUrlOrClientSecret;
    private boolean requiresAction;
}
