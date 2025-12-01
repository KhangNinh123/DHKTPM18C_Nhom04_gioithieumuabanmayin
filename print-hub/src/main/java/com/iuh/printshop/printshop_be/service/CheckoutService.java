package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.checkout.CheckoutRequest;
import com.iuh.printshop.printshop_be.dto.checkout.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse createOrder(Integer userId, CheckoutRequest request);
}
