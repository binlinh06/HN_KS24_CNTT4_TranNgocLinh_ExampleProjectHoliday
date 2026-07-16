package com.phobo.management.review.service;

import com.phobo.management.common.enums.OrderStatus;
import com.phobo.management.common.enums.ReviewModerationStatus;
import com.phobo.management.entity.CustomerProfile;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.entity.Review;
import com.phobo.management.exception.ReviewException;
import com.phobo.management.review.dto.ReviewRequest;
import com.phobo.management.review.dto.ReviewResponse;
import com.phobo.management.repository.CustomerProfileRepository;
import com.phobo.management.repository.OrderEntityRepository;
import com.phobo.management.repository.ReviewRepository;
import com.phobo.management.security.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderEntityRepository orderRepository;
    private final CustomerProfileRepository customerProfileRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            OrderEntityRepository orderRepository,
            CustomerProfileRepository customerProfileRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.customerProfileRepository = customerProfileRepository;
    }

    private CustomerProfile getCurrentCustomerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ReviewException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new ReviewException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ReviewException("Không tìm thấy hồ sơ khách hàng của tài khoản này", "CUSTOMER_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    @Transactional
    public ReviewResponse createReview(String orderId, ReviewRequest request) {
        CustomerProfile customer = getCurrentCustomerProfile();

        // 1. Load Order with PESSIMISTIC_WRITE lock to prevent concurrent reviews for same order
        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ReviewException("Không tìm thấy đơn hàng này", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 2. Verify ownership
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ReviewException("Bạn không có quyền đánh giá đơn hàng này", "ORDER_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        // 3. Verify status is HOAN_THANH (Completed)
        if (order.getStatus() != OrderStatus.HOAN_THANH) {
            throw new ReviewException("Chỉ được đánh giá đơn hàng đã hoàn thành", "ORDER_NOT_COMPLETED", HttpStatus.BAD_REQUEST);
        }

        // 4. Verify no review already exists
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new ReviewException("Đơn hàng này đã được đánh giá trước đó", "REVIEW_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
        }

        // 5. Build and save Review
        Review review = Review.builder()
                .id(UUID.randomUUID().toString())
                .order(order)
                .customer(customer)
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment().trim() : null)
                .moderationStatus(ReviewModerationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewByOrder(String orderId) {
        CustomerProfile customer = getCurrentCustomerProfile();
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ReviewException("Không tìm thấy đơn hàng này", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ReviewException("Bạn không có quyền xem đánh giá của đơn hàng này", "ORDER_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        Review review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ReviewException("Chưa có đánh giá nào cho đơn hàng này", "REVIEW_NOT_FOUND", HttpStatus.NOT_FOUND));

        return mapToResponse(review);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .moderationStatus(review.getModerationStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
