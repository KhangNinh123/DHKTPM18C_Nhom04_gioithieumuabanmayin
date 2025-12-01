package com.iuh.printshop.printshop_be.service.impl;

import com.iuh.printshop.printshop_be.dto.checkout.CheckoutRequest;
import com.iuh.printshop.printshop_be.dto.checkout.CheckoutResponse;
import com.iuh.printshop.printshop_be.entity.*;
import com.iuh.printshop.printshop_be.repository.*;
import com.iuh.printshop.printshop_be.service.CheckoutService;
import com.iuh.printshop.printshop_be.vnpay.VnpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VnpayService vnpayService;
    private final CartItemRepository cartItemRepository;

    @Override
    public CheckoutResponse createOrder(Integer userId, CheckoutRequest req) {

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        if (cart.getItems().isEmpty())
            throw new RuntimeException("Giỏ hàng không có sản phẩm");

// 1. Tạo Order
        Order order = new Order();
        order.setCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUser(cart.getUser());
        order.setFullName(req.getFullName());
        order.setPhone(req.getPhone());
        order.setShippingAddress(req.getShippingAddress());
        order.setPaymentMethod(PaymentMethod.valueOf(req.getPaymentMethod()));

// 2. Order items (KHÔNG DÙNG LAMBDA)
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = OrderItem.builder()
                    .product(ci.getProduct())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getPriceAtAdd())
                    .build();

            order.addItem(oi); // Tự set order vào oi
        }

// 3. Tính tổng
        order.recalcTotal();

// 4. Lưu vào DB
        order = orderRepository.save(order);


        // 2. Nếu COD → trả về luôn
        CheckoutResponse res = new CheckoutResponse();
        res.setOrderId(order.getId());
        res.setOrderCode(order.getCode());

        if (order.getPaymentMethod() == PaymentMethod.COD) {
            res.setRedirectUrl(null);
            return res;
        }

        // 3. Nếu VNPAY → tạo redirect URL
        String vnpUrl = vnpayService.createPaymentUrl(order);
        res.setRedirectUrl(vnpUrl);

        return res;
    }
}
