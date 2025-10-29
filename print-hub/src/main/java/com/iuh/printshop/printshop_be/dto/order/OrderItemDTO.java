package com.iuh.printshop.printshop_be.dto.order;

import lombok.Data;

@Data
public class OrderItemDTO {
    private int productId;
    private String productName;
    private double price;
    private int quantity;
}
