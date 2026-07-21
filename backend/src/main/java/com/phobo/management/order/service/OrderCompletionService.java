package com.phobo.management.order.service;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.OrderType;
import com.phobo.management.common.enums.RestaurantTableStatus;
import com.phobo.management.common.enums.TableSessionStatus;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.entity.RestaurantTable;
import com.phobo.management.entity.TableSession;
import com.phobo.management.repository.OrderEntityRepository;
import com.phobo.management.repository.KitchenQueueRepository;
import com.phobo.management.repository.TableSessionRepository;
import com.phobo.management.repository.RestaurantTableRepository;
import com.phobo.management.repository.PaymentRepository;
import com.phobo.management.entity.Payment;
import com.phobo.management.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCompletionService {

    private final OrderEntityRepository orderRepository;
    private final OrderStatusTransitionService transitionService;
    private final KitchenQueueRepository kitchenQueueRepository;
    private final TableSessionRepository tableSessionRepository;
    private final RestaurantTableRepository tableRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void tryComplete(OrderEntity order) {
        if (order == null) {
            return;
        }

        OrderType type = order.getOrderType();
        if (type == OrderType.ONLINE) {
            // Online order completion follows the delivery flow, not POS completion
            return;
        }

        // Get current user context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = "SYSTEM";
        String role = "SYSTEM";
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal) {
            CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
            userId = principal.getId();
            role = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .findFirst()
                    .orElse("ROLE_STAFF");
        }

        // Check kitchen ready condition
        long itemsInQueue = kitchenQueueRepository.countItemsInQueue(order.getId());
        long nonReadyItems = kitchenQueueRepository.countNonReadyItems(order.getId());
        boolean kitchenReady = itemsInQueue > 0 && nonReadyItems == 0;

        // Check payment condition
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        boolean paymentPaid = payment != null 
                && payment.getPaymentStatus() == com.phobo.management.common.enums.PaymentStatus.SUCCESS;

        boolean isDineIn = order.getTable() != null;

        if (isDineIn) {
            boolean served = order.getServedAt() != null;
            log.info("Dine-In completion check for Order {}: paymentPaid={}, kitchenReady={}, served={}", 
                    order.getId(), paymentPaid, kitchenReady, served);
            
            if (paymentPaid && kitchenReady && served) {
                // Transition Order to HOAN_THANH
                transitionService.transitionStatus(order, OrderStatus.HOAN_THANH, userId, role, "STAFF", "Đơn hàng dine-in hoàn thành");
                
                // Close Table Session
                tableSessionRepository.findByOrderId(order.getId()).ifPresent(session -> {
                    if (session.getStatus() == TableSessionStatus.OPEN || session.getStatus() == TableSessionStatus.PAYMENT_PENDING) {
                        session.setStatus(TableSessionStatus.CLOSED);
                        session.setClosedAt(LocalDateTime.now());
                        tableSessionRepository.save(session);
                        log.info("Closed table session {} for table {}", session.getId(), session.getTable().getId());
                    }
                });

                // Update table status to CLEANING
                if (order.getTable() != null) {
                    RestaurantTable table = tableRepository.findByIdWithLock(order.getTable().getId())
                            .orElse(order.getTable());
                    table.setStatus(RestaurantTableStatus.CLEANING);
                    table.setStatusUpdatedAt(LocalDateTime.now());
                    tableRepository.save(table);
                    log.info("Updated table {} status to CLEANING", table.getId());
                }
            }
        } else { // Takeaway POS
            boolean handedOver = order.getHandedOverAt() != null;
            log.info("POS Takeaway completion check for Order {}: paymentPaid={}, kitchenReady={}, handedOver={}", 
                    order.getId(), paymentPaid, kitchenReady, handedOver);

            if (paymentPaid && kitchenReady && handedOver) {
                // Transition Order to HOAN_THANH
                transitionService.transitionStatus(order, OrderStatus.HOAN_THANH, userId, role, "STAFF", "Đơn hàng takeaway hoàn thành");
            }
        }
    }
}
