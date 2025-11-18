package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCode(String code);

    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT o FROM Order o WHERE o.code = :code OR o.phone = :phone")
    List<Order> findByCodeOrPhone(@Param("code") String code, @Param("phone") String phone);

    List<Order> findAllByOrderByCreatedAtDesc();
}

