package com.iuh.printshop.printshop_be.vnpay;

import com.iuh.printshop.printshop_be.entity.Order;

import java.util.Map;

public interface VnpayService {
    String createPaymentUrl(Order order);
    boolean verifyCallback(Map<String, String> params);
}
