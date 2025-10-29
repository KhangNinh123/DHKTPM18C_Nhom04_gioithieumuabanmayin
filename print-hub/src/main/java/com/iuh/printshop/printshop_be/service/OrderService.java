package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.order.OrderDTO;
import com.iuh.printshop.printshop_be.dto.order.OrderItemDTO;
import com.iuh.printshop.printshop_be.entity.Order;
import com.iuh.printshop.printshop_be.entity.OrderItems;
import com.iuh.printshop.printshop_be.repository.OrderItemRepository;
import com.iuh.printshop.printshop_be.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public List<OrderDTO> findOrderByUserId(int userId) {
        return orderRepository.findOrderByUserId(userId).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public Optional<OrderDTO> findByOrderId(long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDTO);
    }

    public List<OrderDTO> findAll() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCode(order.getCode());
        dto.setFullName(order.getFullName());
        dto.setPhone(order.getPhone());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setSubtotal(order.getSubtotal());
        dto.setTotal(order.getTotal());
        dto.setCreatedDate(order.getCreatedAt());
        dto.setItems(order.getOrderItems().stream().map(item -> {
            OrderItemDTO i = new OrderItemDTO();
            i.setProductId(item.getProduct().getId());
            i.setProductName(item.getProduct().getName());
            i.setPrice(item.getPrice());
            i.setQuantity(item.getQuantity());
            return i;
        }).toList());
        return dto;
    }
}
