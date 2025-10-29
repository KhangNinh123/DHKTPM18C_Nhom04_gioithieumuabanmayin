package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById (long orderId);
    List<Order> findOrderByUserId(int userId);
}