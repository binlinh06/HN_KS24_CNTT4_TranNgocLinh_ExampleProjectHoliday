package com.phobo.management.pos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.PaymentStatus;
import com.phobo.management.common.enums.TableSessionStatus;
import com.phobo.management.entity.*;
import com.phobo.management.exception.OrderException;
import com.phobo.management.pos.dto.PosOrderResponse;
import com.phobo.management.pos.dto.PosPaymentRequest;
import com.phobo.management.repository.*;
import com.phobo.management.security.CustomUserPrincipal;
import com.phobo.management.order.service.OrderCompletionService;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosPaymentService {

    private final OrderEntityRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final OrderCompletionService completionService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final TableSessionRepository tableSessionRepository;
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

    private String calculateHash(PosPaymentRequest request) {
        try {
            String cashStr = request.getCashReceived() != null ? request.getCashReceived().toPlainString() : "";
            String refStr = request.getTerminalReference() != null ? request.getTerminalReference() : "";
            String chanStr = request.getPaymentChannel() != null ? request.getPaymentChannel() : "";
            String input = cashStr + ":" + refStr + ":" + chanStr;
            
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

    private String generateInvoiceNumber(OrderEntity order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = LocalDateTime.now().format(formatter);
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "INV-" + dateStr + "-" + uniquePart;
    }

    @Transactional
    public PosOrderResponse processPosPayment(String orderId, PosPaymentRequest request, String idempotencyKey) {
        // Validate Idempotency-Key
        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new OrderException("Idempotency-Key không đúng định dạng UUID", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        EmployeeProfile employee = getCurrentEmployeeProfile();
        String requestHash = calculateHash(request);

        // Claim Idempotency Key
        int affectedRows = idempotencyRecordRepository.claimIdempotencyKey(
                UUID.randomUUID().toString(),
                null,
                idempotencyKey,
                "PROCESS_POS_PAYMENT",
                requestHash,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24)
        );

        if (affectedRows == 0) {
            IdempotencyRecord record = idempotencyRecordRepository.findByIdempotencyKeyAndOperation(
                    null, "PROCESS_POS_PAYMENT", idempotencyKey
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
                    return processPosPayment(orderId, request, idempotencyKey);
                } else {
                    throw new OrderException("Yêu cầu xử lý thanh toán đang được tiến hành", "IDEMPOTENCY_REQUEST_IN_PROGRESS", HttpStatus.CONFLICT);
                }
            }
        }

        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderException("Không tìm thấy đơn hàng", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment == null) {
            idempotencyRecordRepository.deleteById(idempotencyKey);
            throw new OrderException("Đơn hàng không có bản ghi thanh toán", "PAYMENT_NOT_FOUND", HttpStatus.BAD_REQUEST);
        }

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            // Already paid, return success response
            PosOrderResponse response = mapToResponse(order, payment);
            saveIdempotencyResponse(idempotencyKey, response);
            return response;
        }

        BigDecimal finalAmount = order.getFinalAmount();
        BigDecimal cashReceived = request.getCashReceived() != null ? request.getCashReceived() : BigDecimal.ZERO;
        BigDecimal changeAmount = BigDecimal.ZERO;

        if (order.getPaymentMethod() == PaymentMethod.CASH) {
            if (request.getCashReceived() == null) {
                idempotencyRecordRepository.deleteById(idempotencyKey);
                throw new OrderException("Số tiền khách đưa không được để trống đối với thanh toán tiền mặt", "CASH_RECEIVED_REQUIRED", HttpStatus.BAD_REQUEST);
            }
            if (cashReceived.compareTo(finalAmount) < 0) {
                idempotencyRecordRepository.deleteById(idempotencyKey);
                throw new OrderException("Số tiền khách đưa nhỏ hơn số tiền cần thanh toán", "INSUFFICIENT_CASH", HttpStatus.BAD_REQUEST);
            }
            changeAmount = cashReceived.subtract(finalAmount);
        }

        // Process Payment Success
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCashReceived(cashReceived);
        payment.setChangeAmount(changeAmount);
        payment.setReceivedBy(employee);
        payment.setTerminalReference(request.getTerminalReference());
        payment.setPaymentChannel(request.getPaymentChannel());
        paymentRepository.save(payment);

        // Generate Invoice
        Invoice invoice = Invoice.builder()
                .id(UUID.randomUUID().toString())
                .invoiceNumber(generateInvoiceNumber(order))
                .order(order)
                .payment(payment)
                .issuedBy(employee)
                .subtotal(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(finalAmount)
                .paymentMethod(order.getPaymentMethod().name())
                .issuedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .printCount(0)
                .build();
        invoiceRepository.save(invoice);

        // Update active table session if applicable to PAYMENT_PENDING
        tableSessionRepository.findByOrderId(order.getId()).ifPresent(session -> {
            if (session.getStatus() == TableSessionStatus.OPEN) {
                session.setStatus(TableSessionStatus.PAYMENT_PENDING);
                tableSessionRepository.save(session);
            }
        });

        // Trigger order completion orchestration
        completionService.tryComplete(order);

        PosOrderResponse response = mapToResponse(order, payment);
        saveIdempotencyResponse(idempotencyKey, response);

        return response;
    }

    private PosOrderResponse mapToResponse(OrderEntity order, Payment payment) {
        return PosOrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .tableId(order.getTable() != null ? order.getTable().getId() : null)
                .subtotal(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .status(order.getStatus())
                .build();
    }

    private void saveIdempotencyResponse(String idempotencyKey, PosOrderResponse response) {
        IdempotencyRecord record = idempotencyRecordRepository.findByIdempotencyKeyAndOperation(
                null, "PROCESS_POS_PAYMENT", idempotencyKey
        ).orElse(null);
        if (record != null) {
            try {
                record.setStatus("COMPLETED");
                record.setResponseBody(objectMapper.writeValueAsString(response));
                idempotencyRecordRepository.save(record);
            } catch (Exception e) {
                log.error("Failed to cache payment idempotency response", e);
            }
        }
    }
}
