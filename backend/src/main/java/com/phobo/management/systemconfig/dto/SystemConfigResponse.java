package com.phobo.management.systemconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {
    private String id;
    private String configKey;
    private String configValue;
    private String configGroup;
    private String description;
    private String valueType;
    private Boolean isPublic;
}
