package com.phobo.management.review.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.review.dto.ReviewRequest;
import com.phobo.management.review.dto.ReviewResponse;
import com.phobo.management.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/review")
@PreAuthorize("hasRole('CUSTOMER')")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable String orderId,
            @Validated @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Gửi đánh giá thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(@PathVariable String orderId) {
        ReviewResponse response = reviewService.getReviewByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy đánh giá thành công"));
    }
}
