package com.iuh.printshop.printshop_be.service.impl;

import com.iuh.printshop.printshop_be.entity.Cart;
import com.iuh.printshop.printshop_be.entity.CartItem;
import com.iuh.printshop.printshop_be.repository.CartRepository;
import com.iuh.printshop.printshop_be.dto.payment.CheckoutInitResponse;
import com.iuh.printshop.printshop_be.dto.payment.CheckoutRequest;
import com.iuh.printshop.printshop_be.dto.payment.InitPaymentResult;
import com.iuh.printshop.printshop_be.entity.*;
import com.iuh.printshop.printshop_be.repository.OrderRepository;
import com.iuh.printshop.printshop_be.repository.PaymentRepository;
import com.iuh.printshop.printshop_be.service.CheckoutService;
import com.iuh.printshop.printshop_be.service.PaymentGateway;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final Map<PaymentMethod, PaymentGateway> paymentGateways;

    @Transactional
    @Override
    public CheckoutInitResponse checkout(Integer userId, CheckoutRequest request) {
        Cart cart = cartRepository.findByUser(User.builder().id(userId).build())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user=" + userId));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Snapshot Cart -> Order
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> snapshot = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getPriceAtAdd())
                    .id(new OrderItemId(order.getId().longValue(), ci.getProduct().getId()))
                    .build();
            snapshot.add(oi);
        }
        order.setItems(snapshot);
        order.recalcTotal();
        order = orderRepository.save(order);

        // Create Payment
        Payment payment = Payment.builder()
                .order(order)
                .method(request.getMethod())
                .status(request.getMethod() == PaymentMethod.COD ? PaymentStatus.COD_PENDING : PaymentStatus.UNPAID)
                .amount(order.getTotal())
                .currency(order.getCurrency())
                .build();

        // Init online gateway
        if (request.getMethod() != PaymentMethod.COD) {
            PaymentGateway gw = paymentGateways.get(request.getMethod());
            if (gw == null) throw new IllegalArgumentException("No gateway for method=" + request.getMethod());
            InitPaymentResult r = gw.initPayment(order, request.getSuccessUrl(), request.getCancelUrl());
            payment.setProviderTransactionId(r.getProviderTxId());
            payment.setCheckoutUrlOrClientSecret(r.getCheckoutUrlOrClientSecret());
            if (r.isRequiresAction()) payment.setStatus(PaymentStatus.REQUIRES_ACTION);
        }

        payment = paymentRepository.save(payment);

        return CheckoutInitResponse.builder()
                .orderId(order.getId())
                .paymentId(payment.getId())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paymentUrlOrClientSecret(payment.getCheckoutUrlOrClientSecret())
                .amount(payment.getAmount())
                .build();
    }
}
