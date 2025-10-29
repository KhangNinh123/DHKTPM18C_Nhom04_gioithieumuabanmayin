package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.order.OrderDTO;
import com.iuh.printshop.printshop_be.entity.Order;
import com.iuh.printshop.printshop_be.entity.OrderItems;
import com.iuh.printshop.printshop_be.entity.OrderItemsId;
import com.iuh.printshop.printshop_be.entity.Product;
import com.iuh.printshop.printshop_be.repository.OrderRepository;
import com.iuh.printshop.printshop_be.repository.ProductRepository;
import com.iuh.printshop.printshop_be.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        for (OrderItems item : order.getOrderItems()) {
            item.setOrder(order);

            Product managedProduct = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProduct().getId()));
            item.setProduct(managedProduct);

            item.setId(new OrderItemsId(order.getId(), managedProduct.getId()));
        }

        double subTotal = order.getOrderItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        order.setSubtotal(subTotal);
        order.setTotal(subTotal + order.getShippingFee());

        Order savedOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> findOrderByUserId (@PathVariable int userId) {
        List<OrderDTO> orders = orderService.findOrderByUserId(userId); // returns a List<OrderDTO>
        if (orders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> findOrderById(@PathVariable long orderId) {
        return orderService.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> findAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }
}
