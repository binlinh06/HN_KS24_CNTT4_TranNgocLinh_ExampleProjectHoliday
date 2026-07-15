package com.phobo.management.address.controller;

import com.phobo.management.address.dto.AddressRequest;
import com.phobo.management.address.dto.AddressResponse;
import com.phobo.management.address.service.AddressService;
import com.phobo.management.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@PreAuthorize("hasRole('CUSTOMER')")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses() {
        List<AddressResponse> responses = addressService.getAddresses();
        return ResponseEntity.ok(ApiResponse.success(responses, "Lấy danh sách địa chỉ thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(@PathVariable String id) {
        AddressResponse response = addressService.getAddress(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết địa chỉ thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Validated @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Thêm địa chỉ mới thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable String id,
            @Validated @RequestBody AddressRequest request) {
        AddressResponse response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật địa chỉ thành công"));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(@PathVariable String id) {
        AddressResponse response = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Đặt địa chỉ mặc định thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable String id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa địa chỉ thành công"));
    }
}
