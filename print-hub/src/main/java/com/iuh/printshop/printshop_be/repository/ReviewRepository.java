package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Integer productId);
    
    Optional<Review> findByProductIdAndUserId(Integer productId, Integer userId);
    
    List<Review> findByUserId(Integer userId);
    
    boolean existsByProductIdAndUserId(Integer productId, Integer userId);
}

