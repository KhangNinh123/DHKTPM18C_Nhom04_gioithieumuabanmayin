package com.iuh.printshop.printshop_be.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class OrderItemsId implements Serializable {
    private long orderId;
    private int productId;
}
