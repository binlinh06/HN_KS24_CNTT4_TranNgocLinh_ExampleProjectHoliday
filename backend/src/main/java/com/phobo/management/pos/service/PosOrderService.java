package com.phobo.management.pos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.OrderType;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.PaymentStatus;
import com.phobo.management.common.enums.TableSessionStatus;
import com.phobo.management.entity.*;
import com.phobo.management.exception.OrderException;
import com.phobo.management.pos.dto.PosOrderItemRequest;
import com.phobo.management.pos.dto.PosOrderRequest;
import com.phobo.management.pos.dto.PosOrderResponse;
import com.phobo.management.repository.*;
import com.phobo.management.security.CustomUserPrincipal;
import com.phobo.management.order.service.OrderStatusTransitionService;
import com.phobo.management.kitchen.service.KitchenQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosOrderService {

    private final OrderEntityRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final RestaurantTableRepository tableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final OrderStatusTransitionService transitionService;
    private final KitchenQueueService kitchenQueueService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    private EmployeeProfile getCurrentEmployeeProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new OrderException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new OrderException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return employeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OrderException("Không tìm thấy hồ sơ nhân viên", "EMPLOYEE_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    private String calculateHash(PosOrderRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(request.getTableId() != null ? request.getTableId() : "").append(":");
            sb.append(request.getPaymentMethod() != null ? request.getPaymentMethod().name() : "").append(":");
            sb.append(request.getDiscountAmount() != null ? request.getDiscountAmount().toPlainString() : "0.00").append(":");
            if (request.getItems() != null) {
                // Sort items by product ID to make hash independent of item ordering
                List<PosOrderItemRequest> sortedItems = new ArrayList<>(request.getItems());
                sortedItems.sort(Comparator.comparing(PosOrderItemRequest::getProductId));
                for (PosOrderItemRequest item : sortedItems) {
                    sb.append(item.getProductId()).append("-").append(item.getQuantity()).append("-");
                    if (item.getOptionIds() != null) {
                        List<String> sortedOpts = new ArrayList<>(item.getOptionIds());
                        Collections.sort(sortedOpts);
                        for (String optId : sortedOpts) {
                            sb.append(optId).append(",");
                        }
                    }
                    sb.append(";");
                }
            }
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new OrderException("Lỗi tính request hash", "HASH_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateUniqueOrderCode() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = LocalDateTime.now().format(formatter);
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PB-" + dateStr + "-" + uniquePart;
    }

    @Transactional(readOnly = true)
    public PosOrderResponse previewOrder(PosOrderRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (PosOrderItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new OrderException("Không tìm thấy sản phẩm " + itemReq.getProductId(), "PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND));
                
                if (!product.getIsAvailable()) {
                    throw new OrderException("Sản phẩm " + product.getProductName() + " đã ngừng phục vụ", "PRODUCT_NOT_AVAILABLE", HttpStatus.BAD_REQUEST);
                }

                BigDecimal unitPrice = product.getBasePrice();
                if (itemReq.getOptionIds() != null) {
                    for (String optId : itemReq.getOptionIds()) {
                        ProductOption option = productOptionRepository.findById(optId)
                                .orElseThrow(() -> new OrderException("Không tìm thấy tùy chọn " + optId, "OPTION_NOT_FOUND", HttpStatus.NOT_FOUND));
                        if (!option.getIsAvailable()) {
                            throw new OrderException("Tùy chọn " + option.getOptionName() + " đã ngừng phục vụ", "OPTION_NOT_AVAILABLE", HttpStatus.BAD_REQUEST);
                        }
                        unitPrice = unitPrice.add(option.getIncrementalPrice());
                    }
                }
                subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            }
        }

        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.subtract(discount).max(BigDecimal.ZERO);

        return PosOrderResponse.builder()
                .tableId(request.getTableId())
                .subtotal(subtotal)
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .status(OrderStatus.DANG_CHE_BIEN)
                .build();
    }

    @Transactional
    public PosOrderResponse createOrder(PosOrderRequest request, String idempotencyKey) {
        // Validate Idempotency-Key
        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new OrderException("Idempotency-Key không đúng định dạng UUID", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        EmployeeProfile employee = getCurrentEmployeeProfile();
        String requestHash = calculateHash(request);

        // Claim Idempotency Key (customer_id is set to null in repository call)
        int affectedRows = idempotencyRecordRepository.claimIdempotencyKey(
                UUID.randomUUID().toString(),
                null,
                idempotencyKey,
                "CREATE_POS_ORDER",
                requestHash,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24)
        );

        if (affectedRows == 0) {
            IdempotencyRecord record = idempotencyRecordRepository.findByIdempotencyKeyAndOperation(
                    null, "CREATE_POS_ORDER", idempotencyKey
            ).orElseThrow(() -> new OrderException("Lỗi hệ thống ghi nhận idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

            if (!record.getRequestHash().equals(requestHash)) {
                throw new OrderException("Idempotency key đã được sử dụng cho một yêu cầu khác", "IDEMPOTENCY_KEY_REUSED", HttpStatus.BAD_REQUEST);
            }

            if ("COMPLETED".equals(record.getStatus())) {
                try {
                    return objectMapper.readValue(record.getResponseBody(), PosOrderResponse.class);
                } catch (Exception e) {
                    throw new OrderException("Lỗi đọc dữ liệu cache idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
                    idempotencyRecordRepository.delete(record);
                    return createOrder(request, idempotencyKey);
                } else {
                    throw new OrderException("Yêu cầu tạo đơn hàng đang được xử lý", "IDEMPOTENCY_REQUEST_IN_PROGRESS", HttpStatus.CONFLICT);
                }
            }
        }

        // Deduce OrderType and active TableSession
        OrderType orderType = OrderType.POS;
        RestaurantTable table = null;
        TableSession tableSession = null;

        if (request.getTableId() != null && !request.getTableId().trim().isEmpty()) {
            table = tableRepository.findByIdWithLock(request.getTableId())
                    .orElseThrow(() -> new OrderException("Không tìm thấy bàn ăn", "TABLE_NOT_FOUND", HttpStatus.NOT_FOUND));

            tableSession = tableSessionRepository.findActiveSessionByTableId(table.getId())
                    .orElseThrow(() -> new OrderException("Bàn ăn chưa được mở phiên hoạt động", "TABLE_SESSION_NOT_FOUND", HttpStatus.BAD_REQUEST));

            if (tableSession.getOrder() != null) {
                // Table session already contains an active order
                idempotencyRecordRepository.deleteById(idempotencyKey);
                throw new OrderException("Bàn ăn đang có đơn hàng hoạt động chưa hoàn tất", "ACTIVE_ORDER_EXISTS_ON_SESSION", HttpStatus.BAD_REQUEST);
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID().toString());
        order.setOrderCode(generateUniqueOrderCode());
        order.setOrderType(orderType);
        order.setTable(table);
        order.setStatus(OrderStatus.DANG_CHE_BIEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatusUpdatedAt(LocalDateTime.now());

        if (request.getItems() != null) {
            for (PosOrderItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new OrderException("Không tìm thấy sản phẩm " + itemReq.getProductId(), "PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND));
                
                if (!product.getIsAvailable()) {
                    throw new OrderException("Sản phẩm " + product.getProductName() + " đã ngừng phục vụ", "PRODUCT_NOT_AVAILABLE", HttpStatus.BAD_REQUEST);
                }

                BigDecimal basePrice = product.getBasePrice();
                BigDecimal optionsPrice = BigDecimal.ZERO;
                if (itemReq.getOptionIds() != null) {
                    for (String optId : itemReq.getOptionIds()) {
                        ProductOption option = productOptionRepository.findById(optId)
                                .orElseThrow(() -> new OrderException("Không tìm thấy tùy chọn " + optId, "OPTION_NOT_FOUND", HttpStatus.NOT_FOUND));
                        optionsPrice = optionsPrice.add(option.getIncrementalPrice());
                    }
                }
                BigDecimal unitPrice = basePrice.add(optionsPrice);
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

                OrderItem orderItem = OrderItem.builder()
                        .id(UUID.randomUUID().toString())
                        .order(order)
                        .product(product)
                        .productNameSnapshot(product.getProductName())
                        .basePriceSnapshot(basePrice)
                        .optionsPriceSnapshot(optionsPrice)
                        .unitPriceSnapshot(unitPrice)
                        .priceAtOrder(unitPrice)
                        .lineTotal(lineTotal)
                        .quantity(itemReq.getQuantity())
                        .build();

                List<OrderItemOption> itemOptions = new ArrayList<>();
                if (itemReq.getOptionIds() != null) {
                    for (String optId : itemReq.getOptionIds()) {
                        ProductOption option = productOptionRepository.findById(optId)
                                .orElseThrow(() -> new OrderException("Không tìm thấy tùy chọn " + optId, "OPTION_NOT_FOUND", HttpStatus.NOT_FOUND));
                        if (!option.getIsAvailable()) {
                            throw new OrderException("Tùy chọn " + option.getOptionName() + " đã ngừng phục vụ", "OPTION_NOT_AVAILABLE", HttpStatus.BAD_REQUEST);
                        }

                        OrderItemOption itemOption = OrderItemOption.builder()
                                .id(UUID.randomUUID().toString())
                                .orderItem(orderItem)
                                .option(option)
                                .optionGroupNameSnapshot(option.getGroup().getGroupName())
                                .optionNameSnapshot(option.getOptionName())
                                .incrementalPriceSnapshot(option.getIncrementalPrice())
                                .build();
                        itemOptions.add(itemOption);
                    }
                }
                orderItem.setOptions(itemOptions);
                orderItems.add(orderItem);

                subtotal = subtotal.add(lineTotal);
            }
        }

        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.subtract(discount).max(BigDecimal.ZERO);

        order.setItems(orderItems);
        order.setTotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setFinalAmount(finalAmount);
        order.setPaymentMethod(request.getPaymentMethod());

        // Save order (cascade creates items and options)
        OrderEntity savedOrder = orderRepository.save(order);

        // Associate order with TableSession if DINE_IN
        if (tableSession != null) {
            tableSession.setOrder(savedOrder);
            tableSessionRepository.save(tableSession);
            log.info("Associated order {} with table session {}", savedOrder.getId(), tableSession.getId());
        }

        // Record initial status history record in the same transaction
        transitionService.recordInitialHistory(savedOrder, "STAFF", employee.getUser().getId(), "STAFF", "Khởi tạo đơn hàng POS trực tiếp tại bếp");

        // Create Payment record
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .order(savedOrder)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .amount(finalAmount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .provider(request.getPaymentMethod().name())
                .build();
        Payment savedPayment = paymentRepository.save(payment);

        // Populate kitchen queue tickets
        kitchenQueueService.ensureQueueEntries(savedOrder);

        // Map response
        PosOrderResponse response = PosOrderResponse.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .tableId(request.getTableId())
                .subtotal(savedOrder.getTotalAmount())
                .discountAmount(savedOrder.getDiscountAmount())
                .finalAmount(savedOrder.getFinalAmount())
                .paymentMethod(savedOrder.getPaymentMethod())
                .paymentStatus(savedPayment.getPaymentStatus())
                .status(savedOrder.getStatus())
                .build();

        // Update Idempotency record
        IdempotencyRecord record = idempotencyRecordRepository.findByIdempotencyKeyAndOperation(
                null, "CREATE_POS_ORDER", idempotencyKey
        ).orElse(null);
        if (record != null) {
            try {
                record.setStatus("COMPLETED");
                record.setResponseBody(objectMapper.writeValueAsString(response));
                idempotencyRecordRepository.save(record);
            } catch (Exception e) {
                log.error("Failed to cache idempotency response", e);
            }
        }

        // Publish NEW_ORDER event after commit
        String eventId = UUID.randomUUID().toString();
        // Construct payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", savedOrder.getId());
        payload.put("orderCode", savedOrder.getOrderCode());
        payload.put("orderType", savedOrder.getOrderType().name());
        payload.put("status", savedOrder.getStatus().name());
        payload.put("finalAmount", savedOrder.getFinalAmount());
        payload.put("tableNumber", table != null ? table.getTableNumber() : null);
        transitionService.recordInitialHistory(savedOrder, "STAFF"); // trigger event listener or send event
        
        return response;
    }
}
