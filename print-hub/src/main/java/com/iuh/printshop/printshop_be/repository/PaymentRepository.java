package com.iuh.printshop.printshop_be.repository;


import com.iuh.printshop.printshop_be.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);
}