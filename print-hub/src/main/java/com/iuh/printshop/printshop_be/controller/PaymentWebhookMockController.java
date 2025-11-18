package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.entity.Payment;
import com.iuh.printshop.printshop_be.entity.PaymentStatus;
import com.iuh.printshop.printshop_be.repository.PaymentRepository;
import com.iuh.printshop.printshop_be.dto.payment.WebhookResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookMockController {

    private final PaymentRepository paymentRepository;

    @GetMapping("/webhook/mock")
    public ResponseEntity<?> webhookMock(
            @RequestParam(name = "txId", required = false) String txId,
            @RequestParam(name = "txn_ref", required = false) String txnRef,
            @RequestParam(name = "status") String status
    ) {

        // Chọn txId hoặc txn_ref
        String finalTxId = (txId != null && !txId.isBlank()) ? txId : txnRef;
        if (finalTxId == null || finalTxId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing txId or txn_ref");
        }

        Payment payment = paymentRepository
                .findByProviderTransactionId(finalTxId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found for txId " + finalTxId)
                );

        String normalized = status.toUpperCase();

        switch (normalized) {
            case "PAID":
                payment.setStatus(PaymentStatus.PAID);

                break;

            case "FAILED":
                payment.setStatus(PaymentStatus.FAILED);
                break;

            default:
                payment.setStatus(PaymentStatus.PENDING);
                break;
        }

        paymentRepository.save(payment);

        WebhookResult result = new WebhookResult();
        result.setPaymentId(payment.getId().longValue());
        result.setNewStatus(payment.getStatus().name());
        result.setMessage("Webhook mock handled successfully");

        return ResponseEntity.ok(result);
    }
}
