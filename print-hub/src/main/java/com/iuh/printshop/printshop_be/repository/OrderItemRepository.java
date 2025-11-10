package com.iuh.printshop.printshop_be.repository;


import com.iuh.printshop.printshop_be.entity.OrderItem;
import com.iuh.printshop.printshop_be.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}
