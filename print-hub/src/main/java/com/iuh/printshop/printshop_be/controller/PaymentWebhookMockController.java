package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.payment.WebhookMockRequest;
import com.iuh.printshop.printshop_be.entity.OrderStatus;
import com.iuh.printshop.printshop_be.entity.Payment;
import com.iuh.printshop.printshop_be.entity.PaymentStatus;
import com.iuh.printshop.printshop_be.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookMockController {
    private final PaymentRepository paymentRepository;

    @PostMapping("/webhook/mock")
    public ResponseEntity<Void> webhookMock(@RequestBody WebhookMockRequest body) {
        Payment payment = paymentRepository.findByProviderTransactionId(body.getTxId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for txId " + body.getTxId()));

        switch (body.getStatus().toUpperCase()) {
            case "PAID" -> {
                payment.setStatus(PaymentStatus.PAID);
                payment.getOrder().setStatus(OrderStatus.PAID);
            }
            case "FAILED" -> {
                payment.setStatus(PaymentStatus.FAILED);
                payment.getOrder().setStatus(OrderStatus.FAILED);
            }
            case "REFUNDED" -> payment.setStatus(PaymentStatus.REFUNDED);
            default -> {}
        }
        paymentRepository.save(payment);
        return ResponseEntity.ok().build();
    }
}
