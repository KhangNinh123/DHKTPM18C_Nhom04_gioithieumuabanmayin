package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.payment.InitPaymentResult;
import com.iuh.printshop.printshop_be.dto.payment.WebhookResult;
import com.iuh.printshop.printshop_be.entity.Order;

import java.util.Map;

public interface PaymentGateway {
    InitPaymentResult initPayment(Order order, String successUrl, String cancelUrl);
    WebhookResult parseAndVerifyWebhook(String rawBody, Map<String, String> headers);
}
