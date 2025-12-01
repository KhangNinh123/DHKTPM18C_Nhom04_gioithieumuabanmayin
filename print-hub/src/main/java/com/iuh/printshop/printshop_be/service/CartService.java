package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.cart.AddToCartRequest;
import com.iuh.printshop.printshop_be.dto.cart.CartResponse;
import com.iuh.printshop.printshop_be.dto.cart.UpdateCartItemRequest;

public interface CartService {
    CartResponse getCart(Integer userId);
    CartResponse addItem(Integer userId, AddToCartRequest req);
    CartResponse updateItem(Integer userId, Integer itemId, UpdateCartItemRequest req);
    CartResponse removeItem(Integer userId, Integer itemId);
    CartResponse clear(Integer userId);

    void clearCart(Integer id);
}

