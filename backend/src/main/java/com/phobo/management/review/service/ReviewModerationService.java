package com.phobo.management.review.service;

import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.common.enums.ReviewModerationStatus;
import com.phobo.management.exception.AppException;
import com.phobo.management.entity.Review;
import com.phobo.management.entity.User;
import com.phobo.management.repository.ReviewRepository;
import com.phobo.management.repository.UserRepository;
import com.phobo.management.review.dto.ReviewDetailResponse;
import com.phobo.management.review.dto.ReviewModerationRequest;
import com.phobo.management.security.EmployeeAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewModerationService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EmployeeAuthorizationService employeeAuthService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<ReviewDetailResponse> getReviewsForModeration(
            ReviewModerationStatus status, Integer rating, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return reviewRepository.findReviewsWithFilters(status, rating, from, to, pageable)
                .map(this::mapToDetailResponse);
    }

    @Transactional
    public ReviewDetailResponse approveReview(String id, ReviewModerationRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        User moderator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin quản lý", "USER_NOT_FOUND"));

        Review review = reviewRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException("Không tìm thấy đánh giá", "REVIEW_NOT_FOUND"));

        review.setModerationStatus(ReviewModerationStatus.APPROVED);
        review.setModeratedBy(moderator);
        review.setModeratedAt(LocalDateTime.now());
        if (request != null && request.getModerationNote() != null) {
            review.setModerationNote(request.getModerationNote().trim());
        }

        reviewRepository.save(review);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "APPROVE_REVIEW", "Review",
                review.getId(), "SUCCESS", "Phê duyệt đánh giá đơn hàng: " + review.getOrder().getId(), null, null);

        return mapToDetailResponse(review);
    }

    @Transactional
    public ReviewDetailResponse rejectReview(String id, ReviewModerationRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        User moderator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin quản lý", "USER_NOT_FOUND"));

        if (request == null || request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new AppException("Lý do từ chối không được để trống", "MODERATION_REASON_REQUIRED");
        }

        Review review = reviewRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException("Không tìm thấy đánh giá", "REVIEW_NOT_FOUND"));

        review.setModerationStatus(ReviewModerationStatus.REJECTED);
        review.setModeratedBy(moderator);
        review.setModeratedAt(LocalDateTime.now());
        review.setRejectionReason(request.getRejectionReason().trim());
        if (request.getModerationNote() != null) {
            review.setModerationNote(request.getModerationNote().trim());
        }

        reviewRepository.save(review);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "REJECT_REVIEW", "Review",
                review.getId(), "SUCCESS", "Từ chối đánh giá đơn hàng: " + review.getOrder().getId() + " - Lý do: " + request.getRejectionReason(), null, null);

        return mapToDetailResponse(review);
    }

    private ReviewDetailResponse mapToDetailResponse(Review r) {
        return ReviewDetailResponse.builder()
                .id(r.getId())
                .orderId(r.getOrder().getId())
                .customerId(r.getCustomer().getId())
                .customerName(r.getCustomer().getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .moderationStatus(r.getModerationStatus())
                .moderatedByUsername(r.getModeratedBy() != null ? r.getModeratedBy().getUsername() : null)
                .moderatedAt(r.getModeratedAt())
                .moderationNote(r.getModerationNote())
                .rejectionReason(r.getRejectionReason())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
