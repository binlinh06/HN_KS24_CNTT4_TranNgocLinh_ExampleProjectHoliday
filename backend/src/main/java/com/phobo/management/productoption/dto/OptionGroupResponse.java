package com.phobo.management.productoption.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupResponse {
    private String id;
    private String groupName;
    private Boolean isRequired;
    private Integer minSelectable;
    private Integer maxSelectable;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OptionResponse> options;
}
