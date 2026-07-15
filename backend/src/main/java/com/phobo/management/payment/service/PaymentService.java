package com.phobo.management.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.PaymentStatus;
import com.phobo.management.entity.CustomerProfile;
import com.phobo.management.entity.IdempotencyRecord;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.entity.Payment;
import com.phobo.management.exception.PaymentException;
import com.phobo.management.payment.dto.PaymentResponse;
import com.phobo.management.payment.gateway.PaymentGateway;
import com.phobo.management.payment.gateway.PaymentGatewayRegistry;
import com.phobo.management.payment.gateway.MockPaymentGateway;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderEntityRepository orderRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderEntityRepository orderRepository,
            CustomerProfileRepository customerProfileRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            PaymentGatewayRegistry gatewayRegistry,
            ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.gatewayRegistry = gatewayRegistry;
        this.objectMapper = objectMapper;
    }

    public String getInfo() {
        return "Payment Service Active";
    }

    private CustomerProfile getCurrentCustomerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PaymentException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new PaymentException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentException("Không tìm thấy hồ sơ khách hàng của tài khoản này", "CUSTOMER_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    private String calculateHash(String orderId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(orderId.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new PaymentException("Lỗi tính toán request hash", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public PaymentResponse initiatePayment(String orderId, String idempotencyKey) {
        // Validate Idempotency-Key
        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new PaymentException("Idempotency-Key không đúng định dạng UUID", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        CustomerProfile customer = getCurrentCustomerProfile();
        String requestHash = calculateHash(orderId);

        // Claim Idempotency key
        int affectedRows = idempotencyRecordRepository.claimIdempotencyKey(
                UUID.randomUUID().toString(),
                customer.getId(),
                idempotencyKey,
                "INITIATE_PAYMENT",
                requestHash,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24)
        );

        if (affectedRows == 0) {
            IdempotencyRecord record = idempotencyRecordRepository.findByCustomerIdAndOperationAndIdempotencyKey(
                    customer.getId(), "INITIATE_PAYMENT", idempotencyKey
            ).orElseThrow(() -> new PaymentException("Lỗi hệ thống ghi nhận idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

            if (!record.getRequestHash().equals(requestHash)) {
                throw new PaymentException("Idempotency key đã được sử dụng cho một yêu cầu khác", "IDEMPOTENCY_KEY_REUSED", HttpStatus.BAD_REQUEST);
            }

            if ("COMPLETED".equals(record.getStatus())) {
                try {
                    return objectMapper.readValue(record.getResponseBody(), PaymentResponse.class);
                } catch (Exception e) {
                    throw new PaymentException("Lỗi đọc dữ liệu cache idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
                    idempotencyRecordRepository.delete(record);
                    return initiatePayment(orderId, idempotencyKey);
                } else {
                    throw new PaymentException("Yêu cầu khởi tạo thanh toán đang được xử lý", "IDEMPOTENCY_REQUEST_IN_PROGRESS", HttpStatus.CONFLICT);
                }
            }
        }

        // Lock order to check status
        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new PaymentException("Không tìm thấy đơn hàng này", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new PaymentException("Bạn không có quyền thanh toán đơn hàng này", "ORDER_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        if (order.getPaymentMethod() != PaymentMethod.ONLINE_GATEWAY) {
            throw new PaymentException("Đơn hàng không sử dụng hình thức thanh toán online", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // Check if there is already a SUCCESS payment
        Optional<Payment> existingSuccess = paymentRepository.findByOrderId(orderId)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS);
        if (existingSuccess.isPresent()) {
            throw new PaymentException("Đơn hàng đã được thanh toán thành công", "PAYMENT_ALREADY_SUCCESS", HttpStatus.BAD_REQUEST);
        }

        // Find or create PENDING payment
        Payment payment = paymentRepository.findByOrderId(orderId)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .orElseGet(() -> {
                    Payment newPayment = Payment.builder()
                            .id(UUID.randomUUID().toString())
                            .order(order)
                            .paymentMethod(PaymentMethod.ONLINE_GATEWAY)
                            .paymentStatus(PaymentStatus.PENDING)
                            .provider("MOCK")
                            .amount(order.getFinalAmount())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return paymentRepository.save(newPayment);
                });

        // Resolve gateway registry and create payment url
        PaymentGateway gateway = gatewayRegistry.getGateway(payment.getProvider());
        String paymentUrl = gateway.createPaymentUrl(order, payment.getId(), idempotencyKey);

        PaymentResponse response = PaymentResponse.builder()
                .orderId(orderId)
                .paymentId(payment.getId())
                .provider(payment.getProvider())
                .paymentUrl(paymentUrl)
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .build();

        // Update Idempotency
        IdempotencyRecord record = idempotencyRecordRepository.findByCustomerIdAndOperationAndIdempotencyKey(
                customer.getId(), "INITIATE_PAYMENT", idempotencyKey
        ).orElseThrow(() -> new PaymentException("Lỗi hệ thống ghi nhận idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            record.setResponseBody(objectMapper.writeValueAsString(response));
            record.setStatus("COMPLETED");
            idempotencyRecordRepository.save(record);
        } catch (Exception e) {
            throw new PaymentException("Lỗi lưu cache kết quả idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Transactional
    public PaymentResponse processCallback(String provider, Map<String, String> queryParams) {
        PaymentGateway gateway = gatewayRegistry.getGateway(provider);
        boolean verified = gateway.verifyCallback(queryParams);
        if (!verified) {
            throw new PaymentException("Chữ ký thanh toán không hợp lệ", "PAYMENT_SIGNATURE_INVALID", HttpStatus.BAD_REQUEST);
        }

        String paymentId = queryParams.get("paymentId");
        String amountStr = queryParams.get("amount");
        String statusStr = queryParams.get("status");
        String orderId = queryParams.get("orderId");

        BigDecimal callbackAmount;
        try {
            callbackAmount = new BigDecimal(amountStr);
        } catch (Exception e) {
            throw new PaymentException("Số tiền không hợp lệ", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // Lock payment with PESSIMISTIC_WRITE
        Payment payment = paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new PaymentException("Không tìm thấy giao dịch thanh toán", "PAYMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        // If SUCCESS (terminal state), ignore and return success immediately
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return mapToResponse(payment);
        }

        if (payment.getAmount().compareTo(callbackAmount) != 0) {
            throw new PaymentException("Số tiền thanh toán không khớp", "PAYMENT_AMOUNT_MISMATCH", HttpStatus.BAD_REQUEST);
        }

        PaymentStatus callbackStatus;
        try {
            callbackStatus = PaymentStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
            throw new PaymentException("Trạng thái giao dịch không hợp lệ", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        if (callbackStatus == PaymentStatus.SUCCESS) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            // Use paymentId or query provider transaction ID if present
            String txId = queryParams.getOrDefault("providerTransactionId", "TX-" + paymentId);
            payment.setProviderTransactionId(txId);

            // Lock and update OrderStatus to DA_XAC_NHAN
            OrderEntity order = orderRepository.findByIdWithLock(payment.getOrder().getId())
                    .orElseThrow(() -> new PaymentException("Không tìm thấy đơn hàng của giao dịch", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));
            order.setStatus(OrderStatus.DA_XAC_NHAN);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        } else {
            // Keep Order Status as CHO_XAC_NHAN to allow retry
            payment.setPaymentStatus(callbackStatus);
            payment.setFailureCode(queryParams.get("failureCode"));
            payment.setFailureMessage(queryParams.get("failureMessage"));
        }

        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetails(String orderId) {
        CustomerProfile customer = getCurrentCustomerProfile();
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentException("Không tìm thấy đơn hàng này", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new PaymentException("Bạn không có quyền xem thông tin thanh toán này", "ORDER_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Chưa khởi tạo giao dịch thanh toán cho đơn hàng này", "PAYMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse simulateMockPayment(String paymentId, String simulatedStatus) {
        // 1. Get current customer
        CustomerProfile customer = getCurrentCustomerProfile();

        // 2. Find Payment
        Payment payment = paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new PaymentException("Không tìm thấy giao dịch thanh toán", "PAYMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 3. Verify Payment belongs to Order of the current customer
        OrderEntity order = orderRepository.findById(payment.getOrder().getId())
                .orElseThrow(() -> new PaymentException("Không tìm thấy đơn hàng của giao dịch", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new PaymentException("Bạn không có quyền thao tác trên thanh toán này", "ORDER_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        // 4. Verify provider is MOCK
        if (!"MOCK".equalsIgnoreCase(payment.getProvider())) {
            throw new PaymentException("Chỉ hỗ trợ mô phỏng cổng thanh toán MOCK", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // 5. Verify payment status is not SUCCESS (SUCCESS is terminal)
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            if ("SUCCESS".equalsIgnoreCase(simulatedStatus)) {
                return mapToResponse(payment);
            }
            throw new PaymentException("Giao dịch thanh toán đã thành công trước đó", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // 6. Verify status in allowed list (SUCCESS, FAILED, CANCELED, EXPIRED)
        String statusInput = simulatedStatus != null ? simulatedStatus.toUpperCase() : "";
        if (!statusInput.equals("SUCCESS") && !statusInput.equals("FAILED") && 
            !statusInput.equals("CANCELED") && !statusInput.equals("EXPIRED")) {
            throw new PaymentException("Trạng thái mô phỏng không hợp lệ", "INVALID_SIMULATED_STATUS", HttpStatus.BAD_REQUEST);
        }

        // 7. Get gateway
        PaymentGateway gateway = gatewayRegistry.getGateway("MOCK");
        if (!(gateway instanceof MockPaymentGateway)) {
            throw new PaymentException("Cổng thanh toán Mock không khả dụng", "PAYMENT_PROVIDER_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE);
        }
        MockPaymentGateway mockGateway = (MockPaymentGateway) gateway;

        // 8. Construct parameters for processCallback
        String amountStr = payment.getAmount().setScale(2).toPlainString();
        String statusParam = statusInput.equals("SUCCESS") ? "SUCCESS" : "FAILED";
        
        // Generate signature server-side
        String signature = mockGateway.generateSignature(amountStr, order.getId(), payment.getId(), statusParam);

        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("orderId", order.getId());
        params.put("amount", amountStr);
        params.put("paymentId", payment.getId());
        params.put("status", statusParam);
        params.put("signature", signature);
        params.put("providerTransactionId", "TX-" + payment.getId());

        if (statusInput.equals("CANCELED")) {
            params.put("failureCode", "CANCELED");
            params.put("failureMessage", "Giao dịch bị hủy bởi người dùng");
        } else if (statusInput.equals("EXPIRED")) {
            params.put("failureCode", "EXPIRED");
            params.put("failureMessage", "Giao dịch hết hạn");
        } else if (statusInput.equals("FAILED")) {
            params.put("failureCode", "FAILED");
            params.put("failureMessage", "Thanh toán thất bại");
        }

        // 9. Call processCallback
        return processCallback("MOCK", params);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .orderId(payment.getOrder().getId())
                .paymentId(payment.getId())
                .provider(payment.getProvider())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .providerTransactionId(payment.getProviderTransactionId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
