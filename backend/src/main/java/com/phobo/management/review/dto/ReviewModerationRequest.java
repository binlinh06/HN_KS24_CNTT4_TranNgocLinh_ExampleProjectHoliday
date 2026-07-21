package com.phobo.management.review.dto;

import lombok.Data;

@Data
public class ReviewModerationRequest {
    private String moderationNote;
    private String rejectionReason;
}
