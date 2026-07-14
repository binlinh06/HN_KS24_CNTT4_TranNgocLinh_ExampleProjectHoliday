package com.phobo.management.review.controller;

import com.phobo.management.review.service.ReviewService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Review.
 * Relevant Use Cases: UC-13, UC-25
 */
@RestController
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-13, UC-25 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(reviewService.getInfo(), "Skeleton active"));
    }
}
