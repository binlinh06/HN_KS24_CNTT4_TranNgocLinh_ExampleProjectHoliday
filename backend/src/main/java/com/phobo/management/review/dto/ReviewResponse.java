package com.phobo.management.review.dto;

import com.phobo.management.common.enums.ReviewModerationStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private String id;
    private String orderId;
    private Integer rating;
    private String comment;
    private ReviewModerationStatus moderationStatus;
    private LocalDateTime createdAt;
}
