package com.phobo.management.review.dto;

import com.phobo.management.common.enums.ReviewModerationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailResponse {
    private String id;
    private String orderId;
    private String customerId;
    private String customerName;
    private Integer rating;
    private String comment;
    private ReviewModerationStatus moderationStatus;
    private String moderatedByUsername;
    private LocalDateTime moderatedAt;
    private String moderationNote;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
