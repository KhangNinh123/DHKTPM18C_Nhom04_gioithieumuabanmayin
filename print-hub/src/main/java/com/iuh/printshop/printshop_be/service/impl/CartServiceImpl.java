package com.iuh.printshop.printshop_be.service.impl;

import com.iuh.printshop.printshop_be.dto.cart.AddToCartRequest;
import com.iuh.printshop.printshop_be.dto.cart.CartItemResponse;
import com.iuh.printshop.printshop_be.dto.cart.CartResponse;
import com.iuh.printshop.printshop_be.dto.cart.UpdateCartItemRequest;
import com.iuh.printshop.printshop_be.entity.*;
import com.iuh.printshop.printshop_be.repository.*;
import com.iuh.printshop.printshop_be.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .total(BigDecimal.ZERO)
                                .build()
                ));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(ci -> {
            return CartItemResponse.builder()
                    .itemId(ci.getId())
                    .productId(ci.getProduct().getId())
                    .productName(ci.getProduct().getName())
                    .unitPrice(ci.getPriceAtAdd())
                    .quantity(ci.getQuantity())
                    .subtotal(ci.getSubtotal())
                    .build();
        }).toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .total(total)
                .build();
    }

    @Override
    public CartResponse getCart(Integer userId) {
        User user = getUser(userId);
        Cart cart = getOrCreateCart(user);
        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(Integer userId, AddToCartRequest req) {
        User user = getUser(userId);
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(0)
                            .priceAtAdd(product.getPrice())
                            .build();
                    cart.getItems().add(newItem);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + req.getQuantity());

        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateItem(Integer userId, Integer itemId, UpdateCartItemRequest req) {
        User user = getUser(userId);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Item không tồn tại"));

        if (req.getQuantity() <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(req.getQuantity());
        }

        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse removeItem(Integer userId, Integer itemId) {
        User user = getUser(userId);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Item không tồn tại"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse clear(Integer userId) {
        User user = getUser(userId);
        Cart cart = getOrCreateCart(user);

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        return toResponse(cartRepository.save(cart));
    }

    @Override
    public void clearCart(Integer id) {}
}
