package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.discount.DiscountApplyRequest;
import com.iuh.printshop.printshop_be.dto.discount.DiscountApplyResponse;
import com.iuh.printshop.printshop_be.dto.order.OrderItemRequest;
import com.iuh.printshop.printshop_be.dto.order.OrderItemResponse;
import com.iuh.printshop.printshop_be.dto.order.OrderRequest;
import com.iuh.printshop.printshop_be.dto.order.OrderResponse;
import com.iuh.printshop.printshop_be.entity.*;
import com.iuh.printshop.printshop_be.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final DiscountService discountService;
    private final CartService cartService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Get current user (if authenticated)
        User user = null;
        try {
            user = userService.getCurrentUser();
        } catch (Exception e) {
            log.debug("User not authenticated, creating guest order");
        }

        // Validate and get products
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    Product product = productRepository.findById(itemRequest.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

                    // Check stock
                    if (product.getStockQuantity() == null || product.getStockQuantity() < itemRequest.getQuantity()) {
                        throw new RuntimeException("Insufficient stock for product: " + product.getName());
                    }

                    return OrderItem.builder()
                            .product(product)
                            .price(product.getPrice())
                            .quantity(itemRequest.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate subtotal
        BigDecimal subtotal = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply discount if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getDiscountCode() != null && !request.getDiscountCode().trim().isEmpty()) {
            try {
                DiscountApplyRequest discountRequest = DiscountApplyRequest.builder()
                        .code(request.getDiscountCode().toUpperCase())
                        .build();
                DiscountApplyResponse discountResponse = discountService.applyDiscount(discountRequest, subtotal);
                discountAmount = discountResponse.getDiscountAmount();
            } catch (Exception e) {
                log.warn("Failed to apply discount code: {}", e.getMessage());
                // Continue without discount
            }
        }

        // Calculate shipping fee
        BigDecimal shippingFee = request.getShippingFee() != null 
                ? request.getShippingFee() 
                : BigDecimal.ZERO;

        // Calculate total
        BigDecimal total = subtotal.subtract(discountAmount).add(shippingFee);

        // Create order
        Order.PaymentMethod paymentMethod;
        try {
            paymentMethod = Order.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            paymentMethod = Order.PaymentMethod.COD;
        }

        Order order = Order.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(paymentMethod)
                .paymentStatus(Order.PaymentStatus.UNPAID)
                .status(Order.OrderStatus.PENDING)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .total(total)
                .build();

        order = orderRepository.save(order);

        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            orderItemRepository.save(item);

            // Deduct stock
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // Clear cart if user is authenticated
        if (user != null) {
            try {
                cartService.clear(user.getId());
            } catch (Exception e) {
                log.warn("Failed to clear cart: {}", e.getMessage());
            }
        }

        return mapToResponse(order);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return mapToResponse(order);
    }

    public OrderResponse getOrderByCode(String code) {
        Order order = orderRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Order not found: " + code));
        return mapToResponse(order);
    }

    public List<OrderResponse> getMyOrders() {
        User user = userService.getCurrentUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> searchOrders(String code, String phone) {
        return orderRepository.findByCodeOrPhone(code, phone).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            
            // If order is cancelled, restore stock
            if (newStatus == Order.OrderStatus.CANCELLED) {
                List<OrderItem> items = orderItemRepository.findByOrderId(id);
                for (OrderItem item : items) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
            
            order = orderRepository.save(order);
            return mapToResponse(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long id, String paymentStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        try {
            Order.PaymentStatus newStatus = Order.PaymentStatus.valueOf(paymentStatus.toUpperCase());
            order.setPaymentStatus(newStatus);
            
            // If paid, update order status to PAID
            if (newStatus == Order.PaymentStatus.PAID) {
                order.setStatus(Order.OrderStatus.PAID);
            }
            
            order = orderRepository.save(order);
            return mapToResponse(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + paymentStatus);
        }
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        
        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .total(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .fullName(order.getFullName())
                .phone(order.getPhone())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod().name())
                .paymentStatus(order.getPaymentStatus().name())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
    public OrderResponse trackOrder(String code) {
        Order order = orderRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + code));
        return mapToResponse(order);
    }

}

