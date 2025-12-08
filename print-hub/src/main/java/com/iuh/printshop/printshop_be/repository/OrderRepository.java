package com.iuh.printshop.printshop_be.repository;

import com.iuh.printshop.printshop_be.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCode(String code);

    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT o FROM Order o WHERE o.code = :code OR o.phone = :phone")
    List<Order> findByCodeOrPhone(@Param("code") String code, @Param("phone") String phone);

    List<Order> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN o.user u
            WHERE
                (
                    :keyword IS NULL
                    OR LOWER(o.code)      LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(o.fullName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(o.phone)     LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR (u IS NOT NULL AND LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
                )
            AND (:status IS NULL         OR o.status = :status)
            AND (:paymentStatus IS NULL  OR o.paymentStatus = :paymentStatus)
            AND (:paymentMethod IS NULL  OR o.paymentMethod = :paymentMethod)
            AND (:fromDate IS NULL       OR o.createdAt >= :fromDate)
            AND (:toDate IS NULL         OR o.createdAt <= :toDate)
            """)
    Page<Order> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") Order.OrderStatus status,
            @Param("paymentStatus") Order.PaymentStatus paymentStatus,
            @Param("paymentMethod") Order.PaymentMethod paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}

