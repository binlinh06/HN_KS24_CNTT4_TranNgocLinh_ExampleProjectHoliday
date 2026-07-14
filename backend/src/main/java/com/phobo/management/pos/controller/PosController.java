package com.phobo.management.pos.controller;

import com.phobo.management.pos.service.PosService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Pos.
 * Relevant Use Cases: UC-17, UC-18, UC-19
 */
@RestController
@RequestMapping("/api/v1/pos")
public class PosController {

    private final PosService posService;

    public PosController(PosService posService) {
        this.posService = posService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-17, UC-18, UC-19 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(posService.getInfo(), "Skeleton active"));
    }
}
