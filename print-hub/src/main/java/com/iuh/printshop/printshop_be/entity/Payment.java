package com.iuh.printshop.printshop_be.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;


@Entity
@Table(name = "payments")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;


    @Column(length = 128)
    private String providerTransactionId;


    @Column(length = 64)
    @Builder.Default
    private String currency = "VND";


    @Column(precision = 16, scale = 2)
    private BigDecimal amount;


    @Column(length = 512)
    private String checkoutUrlOrClientSecret;
}