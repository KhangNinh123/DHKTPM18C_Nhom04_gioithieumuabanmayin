package com.iuh.printshop.printshop_be.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//Order.java

@Entity
@Table(name = "orders")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;


    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal total;


    @Column(length = 64)
    @Builder.Default
    private String currency = "VND";


    @Column(length = 255)
    private String note;


    public void recalcTotal() {
        this.total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}