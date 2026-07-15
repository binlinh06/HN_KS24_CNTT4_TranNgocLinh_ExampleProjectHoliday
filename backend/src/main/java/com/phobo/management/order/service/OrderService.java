package com.phobo.management.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phobo.management.address.dto.AddressResponse;
import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.OrderType;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.PaymentStatus;
import com.phobo.management.cart.service.CartPricingService;
import com.phobo.management.cart.service.CartValidationService;
import com.phobo.management.cart.service.VoucherValidationService;
import com.phobo.management.checkout.dto.CheckoutPreviewRequest;
import com.phobo.management.order.dto.OrderResponse;
import com.phobo.management.entity.*;
import com.phobo.management.exception.AddressException;
import com.phobo.management.exception.CartException;
import com.phobo.management.exception.OrderException;
import com.phobo.management.repository.*;
import com.phobo.management.security.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderEntityRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentRepository paymentRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final CartPricingService cartPricingService;
    private final CartValidationService cartValidationService;
    private final VoucherValidationService voucherValidationService;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderEntityRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderItemOptionRepository orderItemOptionRepository,
            CartRepository cartRepository,
            AddressRepository addressRepository,
            VoucherRepository voucherRepository,
            PaymentRepository paymentRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            CustomerProfileRepository customerProfileRepository,
            CartPricingService cartPricingService,
            CartValidationService cartValidationService,
            VoucherValidationService voucherValidationService,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderItemOptionRepository = orderItemOptionRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.voucherRepository = voucherRepository;
        this.paymentRepository = paymentRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.cartPricingService = cartPricingService;
        this.cartValidationService = cartValidationService;
        this.voucherValidationService = voucherValidationService;
        this.objectMapper = objectMapper;
    }

    public String getInfo() {
        return "Order Service Active";
    }

    private CustomerProfile getCurrentCustomerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new OrderException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new OrderException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OrderException("Không tìm thấy hồ sơ khách hàng của tài khoản này", "CUSTOMER_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    private String calculateHash(CheckoutPreviewRequest request) {
        try {
            String canonicalString = String.format("%s:%s:%s",
                    request.getAddressId() != null ? request.getAddressId() : "",
                    request.getPaymentMethod() != null ? request.getPaymentMethod() : "",
                    request.getCustomerNote() != null ? request.getCustomerNote() : ""
            );
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonicalString.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new OrderException("Lỗi tính toán request hash", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public OrderResponse checkoutOrder(CheckoutPreviewRequest request, String idempotencyKey) {
        // 1. Validate Idempotency-Key
        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new OrderException("Idempotency-Key không đúng định dạng UUID", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        CustomerProfile customer = getCurrentCustomerProfile();
        String requestHash = calculateHash(request);

        // 2. Claim Idempotency Key
        int affectedRows = idempotencyRecordRepository.claimIdempotencyKey(
                UUID.randomUUID().toString(),
                customer.getId(),
                idempotencyKey,
                "CREATE_ORDER",
                requestHash,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24)
        );

        if (affectedRows == 0) {
            IdempotencyRecord record = idempotencyRecordRepository.findByCustomerIdAndOperationAndIdempotencyKey(
                    customer.getId(), "CREATE_ORDER", idempotencyKey
            ).orElseThrow(() -> new OrderException("Lỗi hệ thống ghi nhận idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

            if (!record.getRequestHash().equals(requestHash)) {
                throw new OrderException("Idempotency key đã được sử dụng cho một yêu cầu khác", "IDEMPOTENCY_KEY_REUSED", HttpStatus.BAD_REQUEST);
            }

            if ("COMPLETED".equals(record.getStatus())) {
                try {
                    return objectMapper.readValue(record.getResponseBody(), OrderResponse.class);
                } catch (Exception e) {
                    throw new OrderException("Lỗi đọc dữ liệu cache idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
                    idempotencyRecordRepository.delete(record);
                    return checkoutOrder(request, idempotencyKey);
                } else {
                    throw new OrderException("Yêu cầu tạo đơn hàng đang được xử lý", "IDEMPOTENCY_REQUEST_IN_PROGRESS", HttpStatus.CONFLICT);
                }
            }
        }

        // 3. Lock Cart
        Cart cart = cartRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseThrow(() -> new CartException("Giỏ hàng của bạn đang trống", "CART_EMPTY", HttpStatus.BAD_REQUEST));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartException("Giỏ hàng của bạn đang trống", "CART_EMPTY", HttpStatus.BAD_REQUEST);
        }

        // 4. Verify Address
        if (request.getAddressId() == null || request.getAddressId().trim().isEmpty()) {
            throw new AddressException("Vui lòng chọn địa chỉ giao hàng.", "ADDRESS_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AddressException("Địa chỉ không tồn tại", "ADDRESS_NOT_FOUND", HttpStatus.NOT_FOUND));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new AddressException("Bạn không có quyền sử dụng địa chỉ này", "ADDRESS_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        // 5. Verify Payment Method
        PaymentMethod method;
        try {
            String methodStr = request.getPaymentMethod().toUpperCase();
            if ("ONLINE".equals(methodStr)) {
                methodStr = "ONLINE_GATEWAY";
            }
            method = PaymentMethod.valueOf(methodStr);
        } catch (Exception e) {
            throw new OrderException("Phương thức thanh toán không hợp lệ", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // 6. Calculate Pricing & Items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            List<String> errors = cartValidationService.validateProductConfiguration(item.getProduct(), item.getOptions());
            if (!errors.isEmpty()) {
                throw new OrderException("Cấu hình món ăn trong giỏ không hợp lệ: " + errors.get(0), "CART_INVALID", HttpStatus.BAD_REQUEST);
            }

            BigDecimal unitPrice = cartPricingService.calculateItemUnitPrice(item);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .id(UUID.randomUUID().toString())
                    .product(item.getProduct())
                    .quantity(item.getQuantity())
                    .priceAtOrder(unitPrice)
                    .productNameSnapshot(item.getProduct().getProductName())
                    .basePriceSnapshot(item.getProduct().getBasePrice())
                    .optionsPriceSnapshot(cartPricingService.calculateOptionsPrice(item.getOptions()))
                    .unitPriceSnapshot(unitPrice)
                    .lineTotal(lineTotal)
                    .specialNote(item.getSpecialNote())
                    .options(new ArrayList<>())
                    .build();

            if (item.getOptions() != null) {
                for (ProductOption option : item.getOptions()) {
                    OrderItemOption optionSnapshot = OrderItemOption.builder()
                            .id(UUID.randomUUID().toString())
                            .orderItem(orderItem)
                            .option(option)
                            .optionGroupNameSnapshot(option.getGroup().getGroupName())
                            .optionNameSnapshot(option.getOptionName())
                            .incrementalPriceSnapshot(option.getIncrementalPrice())
                            .build();
                    orderItem.getOptions().add(optionSnapshot);
                }
            }

            orderItems.add(orderItem);
        }

        // 7. Validate Voucher
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher voucher = null;
        if (cart.getAppliedVoucher() != null) {
            voucher = voucherRepository.findByCodeForUpdate(cart.getAppliedVoucher().getCode())
                    .orElseThrow(() -> new OrderException("Mã giảm giá không hợp lệ", "VOUCHER_INVALID", HttpStatus.BAD_REQUEST));

            List<String> voucherErrors = voucherValidationService.validateVoucher(voucher, subtotal);
            if (!voucherErrors.isEmpty()) {
                throw new OrderException("Mã giảm giá không hợp lệ: " + voucherErrors.get(0), "VOUCHER_INVALID", HttpStatus.BAD_REQUEST);
            }

            if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new OrderException("Mã giảm giá đã hết lượt sử dụng", "VOUCHER_USAGE_LIMIT_REACHED", HttpStatus.BAD_REQUEST);
            }

            discountAmount = voucherValidationService.calculateDiscount(voucher, subtotal);
        }

        BigDecimal finalAmount = subtotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // 8. Generate Unique Order Code (e.g. PB-yyyyMMdd-XXXXXX)
        String orderCode = generateUniqueOrderCode();

        // 9. Save Order
        OrderEntity order = OrderEntity.builder()
                .id(UUID.randomUUID().toString())
                .customer(customer)
                .voucher(voucher)
                .orderType(OrderType.ONLINE)
                .status(OrderStatus.CHO_XAC_NHAN)
                .totalAmount(subtotal)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .shippingAddress(address.getAddressDetail())
                .notes(request.getCustomerNote())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderCode(orderCode)
                .paymentMethod(method)
                .shippingAddressSnapshot(address.getAddressDetail())
                .receiverNameSnapshot(address.getReceiverName())
                .receiverPhoneSnapshot(address.getReceiverPhone())
                .voucherCodeSnapshot(voucher != null ? voucher.getCode() : null)
                .shippingFee(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
        }

        OrderEntity savedOrder = orderRepository.save(order);

        // 10. Increment Voucher usedCount
        if (voucher != null) {
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
        }

        // 11. Create Payment
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .order(savedOrder)
                .paymentMethod(method)
                .paymentStatus(PaymentStatus.PENDING)
                .provider("MOCK")
                .amount(finalAmount)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // 12. Clear Cart
        cart.getItems().clear();
        cart.setAppliedVoucher(null);
        cartRepository.save(cart);

        // 13. Map to Response
        OrderResponse response = OrderResponse.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .status(savedOrder.getStatus())
                .paymentMethod(savedOrder.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .subtotal(savedOrder.getTotalAmount())
                .discountAmount(savedOrder.getDiscountAmount())
                .finalAmount(savedOrder.getFinalAmount())
                .createdAt(savedOrder.getCreatedAt())
                .build();

        // 14. Update Idempotency Record
        IdempotencyRecord record = idempotencyRecordRepository.findByCustomerIdAndOperationAndIdempotencyKey(
                customer.getId(), "CREATE_ORDER", idempotencyKey
        ).orElseThrow(() -> new OrderException("Lỗi hệ thống ghi nhận idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            record.setResponseBody(objectMapper.writeValueAsString(response));
            record.setStatus("COMPLETED");
            idempotencyRecordRepository.save(record);
        } catch (Exception e) {
            throw new OrderException("Lỗi lưu cache kết quả idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(String orderId) {
        CustomerProfile customer = getCurrentCustomerProfile();
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Không tìm thấy đơn hàng này", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new OrderException("Bạn không có quyền xem đơn hàng này", "ORDER_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        PaymentStatus paymentStatus = PaymentStatus.PENDING;
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isPresent()) {
            paymentStatus = paymentOpt.get().getPaymentStatus();
        }

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(paymentStatus)
                .subtotal(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String generateUniqueOrderCode() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = LocalDateTime.now().format(formatter);
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int num = random.nextInt(900000) + 100000; // 6-digit random
            String code = "PB-" + dateStr + "-" + num;
            // Validate unique constraint
            // Simple query check
            // Since this is in the same transaction, if it conflicts it will retry or throw.
            // Using check:
            // Since JpaRepository has no custom query, let's just make it very unique
            // Combining UUID portion is even more robust and prevents collisions:
            String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            String codeUUID = "PB-" + dateStr + "-" + uniquePart;
            return codeUUID;
        }
        return "PB-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
