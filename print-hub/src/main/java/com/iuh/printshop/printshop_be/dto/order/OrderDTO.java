package com.iuh.printshop.printshop_be.dto.order;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String code;
    private String fullName;
    private String phone;
    private String shippingAddress;
    private double subtotal;
    private double shippingFee;
    private double total;
    private LocalDate createdDate;
    private List<OrderItemDTO> items;
}
