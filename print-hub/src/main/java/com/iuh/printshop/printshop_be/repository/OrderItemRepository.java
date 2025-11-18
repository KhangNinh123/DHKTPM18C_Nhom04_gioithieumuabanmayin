package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.OrderItem;
import com.iuh.printshop.printshop_be.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    List<OrderItem> findByOrderId(Long orderId);
}

