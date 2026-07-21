package com.phobo.management.kitchen.service;

import com.phobo.management.common.enums.KitchenItemStatus;
import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.entity.KitchenQueue;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.entity.OrderItem;
import com.phobo.management.exception.OrderException;
import com.phobo.management.kitchen.dto.KitchenQueueResponse;
import com.phobo.management.repository.EmployeeProfileRepository;
import com.phobo.management.repository.KitchenQueueRepository;
import com.phobo.management.repository.OrderEntityRepository;
import com.phobo.management.security.CustomUserPrincipal;
import com.phobo.management.order.event.StaffEvent;
import com.phobo.management.order.service.OrderStatusTransitionService;
import com.phobo.management.order.service.OrderCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenQueueService {

    private final KitchenQueueRepository kitchenQueueRepository;
    private final OrderEntityRepository orderRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final OrderStatusTransitionService transitionService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderCompletionService completionService;

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
    public void ensureQueueEntries(OrderEntity order) {
        if (order == null || order.getItems() == null) {
            return;
        }
        log.info("Ensuring kitchen queue entries for order {}", order.getId());
        for (OrderItem item : order.getItems()) {
            if (!kitchenQueueRepository.existsByOrderItemId(item.getId())) {
                KitchenQueue queue = KitchenQueue.builder()
                        .id(UUID.randomUUID().toString())
                        .orderItem(item)
                        .itemStatus(KitchenItemStatus.CHO)
                        .priority(0)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                try {
                    KitchenQueue saved = kitchenQueueRepository.save(queue);
                    
                    // Publish event
                    String eventId = UUID.randomUUID().toString();
                    eventPublisher.publishEvent(new StaffEvent(this, eventId, "KITCHEN_ITEM_CREATED", mapToResponse(saved)));
                    log.info("Queued kitchen item {} for order item {}", saved.getId(), item.getId());
                } catch (DataIntegrityViolationException e) {
                    log.warn("Kitchen queue entry already exists for order item {}, skipping.", item.getId());
                }
            }
        }
    }

    @Transactional
    public KitchenQueueResponse updateItemStatus(String queueId, KitchenItemStatus targetStatus, Long expectedVersion) {
        KitchenQueue queue = kitchenQueueRepository.findByIdWithLock(queueId)
                .orElseThrow(() -> new OrderException("Không tìm thấy món ăn trong hàng đợi", "KITCHEN_ITEM_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (expectedVersion != null && !queue.getVersion().equals(expectedVersion)) {
            throw new OrderException("Dữ liệu bếp đã thay đổi bởi người khác. Vui lòng tải lại.", "OPTIMISTIC_LOCK_FAIL", HttpStatus.CONFLICT);
        }

        KitchenItemStatus currentStatus = queue.getItemStatus();
        if (currentStatus == targetStatus) {
            return mapToResponse(queue); // Idempotency
        }

        // Validate transition CHO -> DANG_NAU -> DA_XONG
        if (currentStatus == KitchenItemStatus.CHO && targetStatus != KitchenItemStatus.DANG_NAU) {
            throw new OrderException("Từ trạng thái Chờ chỉ được chuyển sang Đang nấu", "INVALID_KITCHEN_TRANSITION", HttpStatus.BAD_REQUEST);
        }
        if (currentStatus == KitchenItemStatus.DANG_NAU && targetStatus != KitchenItemStatus.DA_XONG) {
            throw new OrderException("Từ trạng thái Đang nấu chỉ được chuyển sang Đã xong", "INVALID_KITCHEN_TRANSITION", HttpStatus.BAD_REQUEST);
        }
        if (currentStatus == KitchenItemStatus.DA_XONG) {
            throw new OrderException("Món ăn đã hoàn thành không thể chuyển trạng thái khác", "INVALID_KITCHEN_TRANSITION", HttpStatus.BAD_REQUEST);
        }

        EmployeeProfile employee = getCurrentEmployeeProfile();

        queue.setItemStatus(targetStatus);
        queue.setUpdatedAt(LocalDateTime.now());

        OrderEntity order = queue.getOrderItem().getOrder();

        if (targetStatus == KitchenItemStatus.DANG_NAU) {
            queue.setStartedAt(LocalDateTime.now());
            queue.setClaimedBy(employee);
            
            // If order is DA_XAC_NHAN, transition to DANG_CHE_BIEN
            if (order.getStatus() == OrderStatus.DA_XAC_NHAN) {
                transitionService.transitionStatus(order, OrderStatus.DANG_CHE_BIEN, employee.getUser().getId(), "STAFF", "STAFF", "Bắt đầu nấu món ăn");
            }
        } else if (targetStatus == KitchenItemStatus.DA_XONG) {
            queue.setCompletedAt(LocalDateTime.now());
            
            // Save item status change before counting non-ready items
            kitchenQueueRepository.saveAndFlush(queue);

            long nonReady = kitchenQueueRepository.countNonReadyItems(order.getId());
            if (nonReady == 0) {
                // Publish ORDER_ALL_ITEMS_READY event
                String readyEventId = UUID.randomUUID().toString();
                eventPublisher.publishEvent(new StaffEvent(this, readyEventId, "ORDER_ALL_ITEMS_READY", order.getId()));
                log.info("All kitchen items ready for order {}", order.getId());

                // Try to complete order
                completionService.tryComplete(order);
            }
        }

        KitchenQueue saved = kitchenQueueRepository.save(queue);

        // Publish KITCHEN_ITEM_UPDATED event
        String eventId = UUID.randomUUID().toString();
        eventPublisher.publishEvent(new StaffEvent(this, eventId, "KITCHEN_ITEM_UPDATED", mapToResponse(saved)));

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<KitchenQueueResponse> getQueue() {
        return kitchenQueueRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public KitchenQueueResponse mapToResponse(KitchenQueue queue) {
        OrderItem item = queue.getOrderItem();
        OrderEntity order = item.getOrder();

        List<String> options = new ArrayList<>();
        if (item.getOptions() != null) {
            options = item.getOptions().stream()
                    .map(o -> o.getOptionGroupNameSnapshot() + ": " + o.getOptionNameSnapshot())
                    .collect(Collectors.toList());
        }

        return KitchenQueueResponse.builder()
                .id(queue.getId())
                .orderItemId(item.getId())
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .productName(item.getProductNameSnapshot())
                .quantity(item.getQuantity())
                .options(options)
                .itemStatus(queue.getItemStatus())
                .priority(queue.getPriority())
                .claimedByEmployeeId(queue.getClaimedBy() != null ? queue.getClaimedBy().getId() : null)
                .claimedByEmployeeName(queue.getClaimedBy() != null ? queue.getClaimedBy().getFullName() : null)
                .startedAt(queue.getStartedAt())
                .completedAt(queue.getCompletedAt())
                .createdAt(queue.getCreatedAt())
                .version(queue.getVersion())
                .build();
    }
}
