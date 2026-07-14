package com.phobo.management.address.controller;

import com.phobo.management.address.service.AddressService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Skeleton Controller for Address.
 * Relevant Use Cases: UC-04
 */
@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> getPlaceholder() {
        // TODO: Implement business logic for UC-04 in Giai đoạn 2
        return ResponseEntity.ok(ApiResponse.success(addressService.getInfo(), "Skeleton active"));
    }
}
