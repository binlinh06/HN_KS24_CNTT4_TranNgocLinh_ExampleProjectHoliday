package com.phobo.management.cart.controller;

import com.phobo.management.cart.service.CartService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Cart.
 * Relevant Use Cases: UC-07
 */
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-07 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(cartService.getInfo(), "Skeleton active"));
    }
}
