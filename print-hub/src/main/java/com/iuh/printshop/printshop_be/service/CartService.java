package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.cart.AddToCartRequest;
import com.iuh.printshop.printshop_be.dto.cart.CartResponse;
import com.iuh.printshop.printshop_be.dto.cart.UpdateCartItemRequest;

/**
 * Service quản lý giỏ hàng người dùng.
 */
public interface CartService {

    /**
     * Lấy giỏ hàng của user.
     */
    CartResponse getCart(Integer userId);

    /**
     * Thêm sản phẩm vào giỏ hàng.
     */
    CartResponse addItem(Integer userId, AddToCartRequest req);

    /**
     * Cập nhật số lượng item trong giỏ.
     */
    CartResponse updateItem(Integer userId, Integer itemId, UpdateCartItemRequest req);

    /**
     * Xoá một item trong giỏ hàng.
     */
    CartResponse removeItem(Integer userId, Integer itemId);

    /**
     * Người dùng tự nhấn nút xoá toàn bộ giỏ hàng (trong UI).
     */
    CartResponse clear(Integer userId);

    /**
     * Backend tự xoá giỏ hàng sau khi thanh toán thành công.
     * Không trả CartResponse vì không dùng cho UI.
     */
    void clearCart(Integer userId);
}
