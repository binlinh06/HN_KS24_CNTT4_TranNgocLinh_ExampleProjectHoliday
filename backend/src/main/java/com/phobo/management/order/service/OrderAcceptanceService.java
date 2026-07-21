package com.phobo.management.order.service;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.OrderType;
import com.phobo.management.common.enums.PaymentMethod;
import com.phobo.management.common.enums.PaymentStatus;
import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.exception.OrderException;
import com.phobo.management.repository.OrderEntityRepository;
import com.phobo.management.repository.KitchenQueueRepository;
import com.phobo.management.repository.EmployeeProfileRepository;
import com.phobo.management.repository.PaymentRepository;
import com.phobo.management.entity.Payment;
import com.phobo.management.security.CustomUserPrincipal;
import com.phobo.management.kitchen.service.KitchenQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAcceptanceService {

    private final OrderEntityRepository orderRepository;
    private final OrderStatusTransitionService transitionService;
    private final OrderCompletionService completionService;
    private final KitchenQueueRepository kitchenQueueRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final KitchenQueueService kitchenQueueService;
    private final PaymentRepository paymentRepository;

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

    @Transactional
    public void acceptOrder(String orderId) {
        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderException("Không tìm thấy đơn hàng", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (order.getOrderType() != OrderType.ONLINE) {
            throw new OrderException("Chỉ chấp nhận đơn hàng online", "INVALID_ORDER_TYPE", HttpStatus.BAD_REQUEST);
        }

        if (order.getStatus() != OrderStatus.CHO_XAC_NHAN) {
            if (order.getStatus() == OrderStatus.DA_XAC_NHAN) {
                return; // Idempotency
            }
            throw new OrderException("Đơn hàng không ở trạng thái chờ xác nhận", "INVALID_ORDER_STATUS", HttpStatus.BAD_REQUEST);
        }

        // Validate payment methods and statuses
        if (order.getPaymentMethod() == PaymentMethod.ONLINE_GATEWAY) {
            Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
            PaymentStatus paymentStatus = payment != null ? payment.getPaymentStatus() : PaymentStatus.PENDING;
            if (paymentStatus == PaymentStatus.PENDING) {
                throw new OrderException("Đơn hàng thanh toán trực tuyến chưa hoàn tất giao dịch", "ORDER_PAYMENT_PENDING", HttpStatus.BAD_REQUEST);
            } else if (paymentStatus != PaymentStatus.SUCCESS) {
                throw new OrderException("Thanh toán đơn hàng thất bại hoặc đã hủy", "ORDER_PAYMENT_NOT_SUCCESSFUL", HttpStatus.BAD_REQUEST);
            }
        }

        EmployeeProfile employee = getCurrentEmployeeProfile();
        order.setAcceptedBy(employee);
        order.setAcceptedAt(LocalDateTime.now());

        // Save first before transition to avoid lock conflicts
        orderRepository.save(order);

        // Transition Order Status to DA_XAC_NHAN
        transitionService.transitionStatus(order, OrderStatus.DA_XAC_NHAN, employee.getUser().getId(), "STAFF", "STAFF", "Chấp nhận đơn hàng online");

        // Generate kitchen queue entries
        kitchenQueueService.ensureQueueEntries(order);
    }

    @Transactional
    public void rejectOrder(String orderId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new OrderException("Lý do từ chối không được để trống", "REJECTION_REASON_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        reason = reason.trim();
        if (reason.length() > 255) {
            throw new OrderException("Lý do từ chối tối đa 255 ký tự", "REJECTION_REASON_TOO_LONG", HttpStatus.BAD_REQUEST);
        }

        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderException("Không tìm thấy đơn hàng", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (order.getStatus() != OrderStatus.CHO_XAC_NHAN) {
            if (order.getStatus() == OrderStatus.DA_HUY) {
                return; // Idempotency
            }
            throw new OrderException("Chỉ được từ chối đơn hàng đang chờ xác nhận", "INVALID_ORDER_STATUS", HttpStatus.BAD_REQUEST);
        }

        // Block rejecting pre-paid online orders
        if (order.getPaymentMethod() == PaymentMethod.ONLINE_GATEWAY) {
            Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
            PaymentStatus paymentStatus = payment != null ? payment.getPaymentStatus() : PaymentStatus.PENDING;
            if (paymentStatus == PaymentStatus.SUCCESS) {
                throw new OrderException("Không thể từ chối đơn hàng đã thanh toán online. Yêu cầu hoàn tiền thủ công.", "PAID_ORDER_REFUND_REQUIRED", HttpStatus.BAD_REQUEST);
            }
        }

        EmployeeProfile employee = getCurrentEmployeeProfile();
        order.setRejectedBy(employee);
        order.setRejectedAt(LocalDateTime.now());
        order.setRejectionReason(reason);

        // Save order
        orderRepository.save(order);

        // Transition to DA_HUY
        transitionService.transitionStatus(order, OrderStatus.DA_HUY, employee.getUser().getId(), "STAFF", "STAFF", "Từ chối đơn hàng: " + reason);
    }

    @Transactional
    public void serveOrder(String orderId) {
        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderException("Không tìm thấy đơn hàng", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        boolean isDineIn = order.getTable() != null;
        if (!isDineIn) {
            throw new OrderException("Thao tác phục vụ chỉ dành cho đơn ăn tại bàn", "INVALID_ORDER_TYPE", HttpStatus.BAD_REQUEST);
        }

        if (order.getServedAt() != null) {
            return; // Idempotency
        }

        // Validate kitchen queue is fully ready
        long itemsInQueue = kitchenQueueRepository.countItemsInQueue(order.getId());
        long nonReadyItems = kitchenQueueRepository.countNonReadyItems(order.getId());
        if (itemsInQueue == 0 || nonReadyItems > 0) {
            throw new OrderException("Chưa thể phục vụ do các món ăn chưa được chế biến xong", "KITCHEN_ITEMS_NOT_READY", HttpStatus.BAD_REQUEST);
        }

        EmployeeProfile employee = getCurrentEmployeeProfile();
        order.setServedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Transition to DANG_PHUC_VU
        transitionService.transitionStatus(order, OrderStatus.DANG_PHUC_VU, employee.getUser().getId(), "STAFF", "STAFF", "Phục vụ món ăn tại bàn");

        // Orchestrate completion check
        completionService.tryComplete(order);
    }

    @Transactional
    public void handoverOrder(String orderId) {
        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderException("Không tìm thấy đơn hàng", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        boolean isTakeaway = order.getOrderType() == OrderType.POS && order.getTable() == null;
        if (!isTakeaway) {
            throw new OrderException("Thao tác bàn giao chỉ dành cho đơn mang về", "INVALID_ORDER_TYPE", HttpStatus.BAD_REQUEST);
        }

        if (order.getHandedOverAt() != null) {
            return; // Idempotency
        }

        // Validate kitchen queue is fully ready
        long itemsInQueue = kitchenQueueRepository.countItemsInQueue(order.getId());
        long nonReadyItems = kitchenQueueRepository.countNonReadyItems(order.getId());
        if (itemsInQueue == 0 || nonReadyItems > 0) {
            throw new OrderException("Chưa thể bàn giao do các món ăn chưa được chế biến xong", "KITCHEN_ITEMS_NOT_READY", HttpStatus.BAD_REQUEST);
        }

        order.setHandedOverAt(LocalDateTime.now());
        orderRepository.save(order);

        // Orchestrate completion check
        completionService.tryComplete(order);
    }
}
