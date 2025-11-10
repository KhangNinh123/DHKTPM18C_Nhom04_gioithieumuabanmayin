package com.iuh.printshop.printshop_be.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderItemId implements Serializable {
    private Long orderId;
    private Integer productId;

    public OrderItemId() {}
    public OrderItemId(Long orderId, Integer productId) {
        this.orderId = orderId;
        this.productId = productId;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItemId that)) return false;
        return Objects.equals(orderId, that.orderId)
                && Objects.equals(productId, that.productId);
    }
    @Override public int hashCode() {
        return Objects.hash(orderId, productId);
    }
}
