package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    Optional<Discount> findByCode(String code);
    
    List<Discount> findByIsActiveTrue();
    
    List<Discount> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        LocalDateTime now, LocalDateTime now2
    );
}


