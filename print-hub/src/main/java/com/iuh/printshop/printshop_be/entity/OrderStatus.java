package com.iuh.printshop.printshop_be.entity;

public enum OrderStatus {
    PENDING,       // chưa xử lý
    CONFIRMED,     // thanh toán thành công
    FAILED,        // thanh toán thất bại
    CANCELED       // người dùng hủy
}
