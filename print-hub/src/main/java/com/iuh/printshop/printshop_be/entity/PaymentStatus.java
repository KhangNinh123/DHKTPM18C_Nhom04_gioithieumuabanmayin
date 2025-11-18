package com.iuh.printshop.printshop_be.entity;

public enum PaymentStatus {
    PENDING,       // trạng thái mặc định
    PAID,          // đã thanh toán
    FAILED,        // thất bại
    REFUNDED,
    COD_PENDING,
    REQUIRES_ACTION, UNPAID
}
