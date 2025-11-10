package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItems, Long> {
    Optional<List<OrderItems>> findByProductId(int productId);
}
