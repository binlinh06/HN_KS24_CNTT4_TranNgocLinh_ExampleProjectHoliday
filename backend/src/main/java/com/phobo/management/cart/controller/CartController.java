package com.phobo.management.cart.controller;

import com.phobo.management.cart.dto.*;
import com.phobo.management.cart.service.CartService;
import com.phobo.management.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(), "Lấy giỏ hàng thành công"));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cartService.addItem(request), "Thêm món vào giỏ hàng thành công"));
    }

    @PatchMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable String cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateItem(cartItemId, request), "Cập nhật sản phẩm thành công"));
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable String cartItemId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.removeItem(cartItemId), "Xóa sản phẩm thành công"));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.clearCart(), "Xóa toàn bộ giỏ hàng thành công"));
    }

    @PostMapping("/voucher")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> applyVoucher(@Valid @RequestBody VoucherApplyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cartService.applyVoucher(request.getCode()), "Áp dụng mã giảm giá thành công"));
    }

    @DeleteMapping("/voucher")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> removeVoucher() {
        return ResponseEntity.ok(ApiResponse.success(cartService.removeVoucher(), "Hủy áp dụng mã giảm giá thành công"));
    }

    @PostMapping("/merge")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartMergeResponse>> mergeCart(
            @Valid @RequestBody List<GuestCartMergeItemRequest> mergeItems,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestParam(value = "mergeRequestId", required = false) String mergeRequestId) {
        
        String key = idempotencyKey != null ? idempotencyKey : mergeRequestId;
        return ResponseEntity.ok(ApiResponse.success(
                cartService.mergeCart(mergeItems, key),
                "Hợp nhất giỏ hàng thành công"
        ));
    }
}
