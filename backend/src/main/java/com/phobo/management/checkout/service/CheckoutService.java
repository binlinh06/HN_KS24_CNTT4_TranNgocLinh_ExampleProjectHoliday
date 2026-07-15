package com.phobo.management.checkout.service;

import com.phobo.management.address.dto.AddressResponse;
import com.phobo.management.cart.dto.CartItemResponse;
import com.phobo.management.cart.dto.CartOptionResponse;
import com.phobo.management.cart.service.CartPricingService;
import com.phobo.management.cart.service.CartValidationService;
import com.phobo.management.cart.service.VoucherValidationService;
import com.phobo.management.checkout.dto.CheckoutPreviewRequest;
import com.phobo.management.checkout.dto.CheckoutPreviewResponse;
import com.phobo.management.entity.*;
import com.phobo.management.exception.AddressException;
import com.phobo.management.exception.CartException;
import com.phobo.management.repository.*;
import com.phobo.management.security.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final CartValidationService cartValidationService;
    private final CartPricingService cartPricingService;
    private final VoucherValidationService voucherValidationService;

    public CheckoutService(
            CartRepository cartRepository,
            AddressRepository addressRepository,
            CustomerProfileRepository customerProfileRepository,
            CartValidationService cartValidationService,
            CartPricingService cartPricingService,
            VoucherValidationService voucherValidationService) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.cartValidationService = cartValidationService;
        this.cartPricingService = cartPricingService;
        this.voucherValidationService = voucherValidationService;
    }

    private CustomerProfile getCurrentCustomerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CartException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new CartException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException("Không tìm thấy hồ sơ khách hàng của tài khoản này", "CUSTOMER_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    @Transactional(readOnly = true)
    public CheckoutPreviewResponse previewCheckout(CheckoutPreviewRequest request) {
        CustomerProfile customer = getCurrentCustomerProfile();
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new CartException("Giỏ hàng của bạn đang trống", "CART_EMPTY", HttpStatus.BAD_REQUEST));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartException("Giỏ hàng của bạn đang trống", "CART_EMPTY", HttpStatus.BAD_REQUEST);
        }

        List<String> warnings = new ArrayList<>();
        boolean valid = true;

        // 1. Verify Address
        AddressResponse addressResponse = null;
        if (request.getAddressId() != null && !request.getAddressId().trim().isEmpty()) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AddressException("Địa chỉ không tồn tại", "ADDRESS_NOT_FOUND", HttpStatus.NOT_FOUND));
            if (!address.getCustomer().getId().equals(customer.getId())) {
                throw new AddressException("Bạn không có quyền sử dụng địa chỉ này", "ADDRESS_FORBIDDEN", HttpStatus.FORBIDDEN);
            }
            addressResponse = AddressResponse.builder()
                    .id(address.getId())
                    .receiverName(address.getReceiverName())
                    .receiverPhone(address.getReceiverPhone())
                    .addressDetail(address.getAddressDetail())
                    .addressLabel(address.getAddressLabel())
                    .isDefault(address.getIsDefault())
                    .createdAt(address.getCreatedAt())
                    .updatedAt(address.getUpdatedAt())
                    .build();
        } else {
            warnings.add("Vui lòng chọn địa chỉ giao hàng.");
            valid = false;
        }

        // 2. Validate Payment Method
        String paymentMethod = request.getPaymentMethod();
        if (paymentMethod == null || (!"COD".equalsIgnoreCase(paymentMethod) && !"ONLINE".equalsIgnoreCase(paymentMethod))) {
            warnings.add("Phương thức thanh toán không hợp lệ.");
            valid = false;
        }

        // 3. Revalidate and calculate items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            List<String> itemErrors = cartValidationService.validateProductConfiguration(item.getProduct(), item.getOptions());
            if (!itemErrors.isEmpty()) {
                warnings.addAll(itemErrors);
                valid = false;
            }

            BigDecimal unitPrice = cartPricingService.calculateItemUnitPrice(item);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            // Convert options to DTO matching CartOptionResponse
            List<CartOptionResponse> options = item.getOptions() == null ? Collections.emptyList() :
                    item.getOptions().stream()
                            .map(o -> CartOptionResponse.builder()
                                    .id(UUID.fromString(o.getId()))
                                    .groupName(o.getGroup() != null ? o.getGroup().getGroupName() : "")
                                    .optionName(o.getOptionName())
                                    .incrementalPrice(o.getIncrementalPrice())
                                    .build())
                            .collect(Collectors.toList());

            CartItemResponse.CartProductResponse prodResponse = CartItemResponse.CartProductResponse.builder()
                    .id(UUID.fromString(item.getProduct().getId()))
                    .name(item.getProduct().getProductName())
                    .slug(item.getProduct().getSlug())
                    .imageUrl(item.getProduct().getImageUrl())
                    .available(Boolean.TRUE.equals(item.getProduct().getIsAvailable()))
                    .build();

            itemResponses.add(CartItemResponse.builder()
                    .id(UUID.fromString(item.getId()))
                    .product(prodResponse)
                    .selectedOptions(options)
                    .quantity(item.getQuantity())
                    .specialNote(item.getSpecialNote())
                    .basePrice(item.getProduct().getBasePrice())
                    .optionsPrice(cartPricingService.calculateOptionsPrice(item.getOptions()))
                    .unitPriceSnapshot(item.getUnitPriceSnapshot())
                    .currentUnitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .priceChanged(item.getUnitPriceSnapshot() != null && item.getUnitPriceSnapshot().compareTo(unitPrice) != 0)
                    .valid(itemErrors.isEmpty())
                    .validationMessages(itemErrors)
                    .build());
        }

        // 4. Revalidate Voucher
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedVoucherCode = null;

        if (cart.getAppliedVoucher() != null) {
            Voucher voucher = cart.getAppliedVoucher();
            List<String> voucherErrors = voucherValidationService.validateVoucher(voucher, subtotal);
            if (!voucherErrors.isEmpty()) {
                warnings.addAll(voucherErrors);
                valid = false;
            } else {
                appliedVoucherCode = voucher.getCode();
                discountAmount = voucherValidationService.calculateDiscount(voucher, subtotal);
            }
        }

        BigDecimal finalAmount = subtotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        return CheckoutPreviewResponse.builder()
                .address(addressResponse)
                .items(itemResponses)
                .subtotal(subtotal)
                .appliedVoucherCode(appliedVoucherCode)
                .discountAmount(discountAmount)
                .shippingFee(BigDecimal.ZERO) // Fixed to 0 for Phase 5
                .finalAmount(finalAmount)
                .paymentMethod(paymentMethod)
                .warnings(warnings)
                .valid(valid)
                .build();
    }
}
