package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    
    Optional<Payment> findByTxnRef(String txnRef);
}

