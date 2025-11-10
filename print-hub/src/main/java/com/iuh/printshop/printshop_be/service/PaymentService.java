package com.iuh.printshop.printshop_be.service;

import java.util.Map;

public interface PaymentService {
    void handleWebhook(String provider, String rawBody, Map<String, String> headers);
}
