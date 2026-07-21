package com.phobo.management.review.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.common.enums.ReviewModerationStatus;
import com.phobo.management.review.dto.ReviewDetailResponse;
import com.phobo.management.review.dto.ReviewModerationRequest;
import com.phobo.management.review.service.ReviewModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/manager/reviews")
@RequiredArgsConstructor
public class ManagerReviewController {

    private final ReviewModerationService reviewModerationService;

    @GetMapping
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<Page<ReviewDetailResponse>>> getReviews(
            @RequestParam(required = false) ReviewModerationStatus status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        int validatedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDetailResponse> result = reviewModerationService.getReviewsForModeration(status, rating, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách kiểm duyệt đánh giá thành công"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> approveReview(
            @PathVariable String id,
            @RequestBody(required = false) ReviewModerationRequest request) {
        ReviewDetailResponse response = reviewModerationService.approveReview(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Phê duyệt đánh giá thành công"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("@employeeAuthService.isManager()")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> rejectReview(
            @PathVariable String id,
            @RequestBody ReviewModerationRequest request) {
        ReviewDetailResponse response = reviewModerationService.rejectReview(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Từ chối đánh giá thành công"));
    }
}
