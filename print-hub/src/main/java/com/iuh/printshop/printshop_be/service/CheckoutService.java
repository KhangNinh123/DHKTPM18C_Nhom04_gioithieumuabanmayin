package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.payment.CheckoutInitResponse;
import com.iuh.printshop.printshop_be.dto.payment.CheckoutRequest;

public interface CheckoutService {
    CheckoutInitResponse checkout(Integer userId, CheckoutRequest request);
}
