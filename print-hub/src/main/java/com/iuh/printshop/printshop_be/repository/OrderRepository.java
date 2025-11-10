package com.iuh.printshop.printshop_be.repository;


import com.iuh.printshop.printshop_be.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Integer> {}
