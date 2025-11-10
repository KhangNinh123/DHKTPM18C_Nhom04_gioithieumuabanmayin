package com.iuh.printshop.printshop_be.service.impl;

import com.iuh.printshop.printshop_be.repository.CartRepository;
import com.iuh.printshop.printshop_be.repository.OrderRepository;
import com.iuh.printshop.printshop_be.repository.PaymentRepository;
import com.iuh.printshop.printshop_be.service.PaymentGateway;
import com.iuh.printshop.printshop_be.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final Map<com.iuh.printshop.printshop_be.entity.PaymentMethod, PaymentGateway> paymentGateways;

    @Transactional
    @Override
    public void handleWebhook(String provider, String rawBody, Map<String, String> headers) {
        throw new UnsupportedOperationException("Implement real provider webhook or use /api/payments/webhook/mock");
    }
}
