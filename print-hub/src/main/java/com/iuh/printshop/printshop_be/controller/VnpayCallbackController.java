package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.entity.*;
import com.iuh.printshop.printshop_be.repository.OrderRepository;
import com.iuh.printshop.printshop_be.service.CartService;
import com.iuh.printshop.printshop_be.vnpay.VnpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class VnpayCallbackController {

    private final OrderRepository orderRepository;
    private final VnpayService vnpayService;
    private final CartService cartService;

    @GetMapping("/return")
    public ResponseEntity<?> returnUrl(@RequestParam Map<String, String> params) {

        String orderCode = params.get("vnp_TxnRef");
        Order order = orderRepository.findByCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!vnpayService.verifyCallback(new HashMap<>(params))) {
            order.markFailed();
            orderRepository.save(order);
            return ResponseEntity.badRequest().body("Checksum invalid");
        }

        if ("00".equals(params.get("vnp_ResponseCode"))) {
            order.markPaid();
            orderRepository.save(order);

            // Clear cart
            if (order.getUser() != null) {
                cartService.clearCart(order.getUser().getId());
            }

            return ResponseEntity.ok("Payment success");
        }

        order.markFailed();
        orderRepository.save(order);

        return ResponseEntity.ok("Payment failed");
    }
}
