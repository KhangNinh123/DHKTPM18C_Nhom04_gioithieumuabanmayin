package com.iuh.printshop.printshop_be.service.impl;

import com.iuh.printshop.printshop_be.dto.checkout.CheckoutRequest;
import com.iuh.printshop.printshop_be.dto.checkout.CheckoutResponse;
import com.iuh.printshop.printshop_be.entity.*;
import com.iuh.printshop.printshop_be.repository.*;
import com.iuh.printshop.printshop_be.service.CheckoutService;
import com.iuh.printshop.printshop_be.vnpay.VnpayService;
import jakarta.transaction.Transactional;
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
    private final CartItemRepository cartItemRepository;
    private final VnpayService vnpayService;

    @Override
    @Transactional
    public CheckoutResponse createOrder(Integer userId, CheckoutRequest req) {

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        if (cart.getItems().isEmpty())
            throw new RuntimeException("Giỏ hàng đang trống");

        // 1. Tạo Order
        Order order = new Order();
        order.setCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUser(cart.getUser());
        order.setFullName(req.getFullName());
        order.setPhone(req.getPhone());
        order.setShippingAddress(req.getShippingAddress());
        order.setPaymentMethod(PaymentMethod.valueOf(req.getPaymentMethod()));

        // 2. Tạo OrderItem
        for (CartItem ci : cart.getItems()) {

            OrderItemId id = new OrderItemId(null, ci.getProduct().getId());

            OrderItem item = OrderItem.builder()
                    .id(id)
                    .order(order)
                    .product(ci.getProduct())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getPriceAtAdd())
                    .price(ci.getPriceAtAdd().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .build();

            order.getItems().add(item);
        }

        // 3. Tính tổng
        order.recalcTotal();

        // 4. Lưu order
        order = orderRepository.save(order);

        // 5. Xoá giỏ hàng
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setTotal(BigDecimal.ZERO);
        cartRepository.save(cart);

        CheckoutResponse res = new CheckoutResponse(order.getId(), order.getCode(), null);

        if (order.getPaymentMethod() == PaymentMethod.VNPAY) {
            String vnpUrl = vnpayService.createPaymentUrl(order);
            res.setRedirectUrl(vnpUrl);
        }

        return res;
    }
}
