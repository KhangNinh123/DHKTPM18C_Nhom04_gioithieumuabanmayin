package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByUser_Id(Integer userId);

    Optional<Cart> findByUser(com.iuh.printshop.printshop_be.entity.User user);

    boolean existsByUser(com.iuh.printshop.printshop_be.entity.User user);
}
