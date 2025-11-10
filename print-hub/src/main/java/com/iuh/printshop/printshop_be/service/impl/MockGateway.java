package com.iuh.printshop.printshop_be.service.impl;

import com.iuh.printshop.printshop_be.dto.payment.InitPaymentResult;
import com.iuh.printshop.printshop_be.dto.payment.WebhookResult;
import com.iuh.printshop.printshop_be.entity.Order;
import com.iuh.printshop.printshop_be.service.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component("MOCK")
public class MockGateway implements PaymentGateway {

    @Override
    public InitPaymentResult initPayment(Order order, String successUrl, String cancelUrl) {
        String txId = "MOCK-" + UUID.randomUUID();
        return InitPaymentResult.builder()
                .providerTxId(txId)
                .checkoutUrlOrClientSecret("about:blank")
                .requiresAction(false)
                .build();
    }

    @Override
    public WebhookResult parseAndVerifyWebhook(String rawBody, Map<String, String> headers) {
        return null; // dùng controller mock để cập nhật
    }
}
