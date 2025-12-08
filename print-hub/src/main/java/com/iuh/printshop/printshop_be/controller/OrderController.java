package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.order.OrderRequest;
import com.iuh.printshop.printshop_be.dto.order.OrderResponse;
import com.iuh.printshop.printshop_be.entity.Order;
import com.iuh.printshop.printshop_be.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order Management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order from cart items")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Get order details by order ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get order by code", description = "Get order details by order code")
    public ResponseEntity<OrderResponse> getOrderByCode(@PathVariable String code) {
        OrderResponse order = orderService.getOrderByCode(code);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/me")
    @Operation(summary = "Get my orders", description = "Get all orders of current authenticated user")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        List<OrderResponse> orders = orderService.getMyOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/search")
    @Operation(summary = "Search orders", description = "Search orders by code or phone number")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String phone) {
        List<OrderResponse> orders = orderService.searchOrders(code, phone);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status", description = "Update order status (Admin only)")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update payment status", description = "Update payment status (Admin only)")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String paymentStatus) {
        OrderResponse order = orderService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders", description = "Get all orders (Admin only)")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search & filter orders (Admin)",
            description = "Search orders with filters, sort and pagination (Admin only)")
    public ResponseEntity<Page<OrderResponse>> searchOrdersAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) Order.PaymentStatus paymentStatus,
            @RequestParam(required = false) Order.PaymentMethod paymentMethod,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<OrderResponse> result = orderService.searchOrdersAdmin(
                keyword,
                status,
                paymentStatus,
                paymentMethod,
                fromDate,
                toDate,
                page,
                size,
                sortBy,
                sortDir
        );
        return ResponseEntity.ok(result);
    }
}

