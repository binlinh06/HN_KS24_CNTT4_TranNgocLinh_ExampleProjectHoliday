package com.phobo.management.address.service;

import com.phobo.management.address.dto.AddressRequest;
import com.phobo.management.address.dto.AddressResponse;
import com.phobo.management.entity.Address;
import com.phobo.management.entity.CustomerProfile;
import com.phobo.management.exception.AddressException;
import com.phobo.management.repository.AddressRepository;
import com.phobo.management.repository.CustomerProfileRepository;
import com.phobo.management.security.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerProfileRepository customerProfileRepository;

    public AddressService(AddressRepository addressRepository, CustomerProfileRepository customerProfileRepository) {
        this.addressRepository = addressRepository;
        this.customerProfileRepository = customerProfileRepository;
    }

    private CustomerProfile getCurrentCustomerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AddressException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new AddressException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AddressException("Không tìm thấy hồ sơ khách hàng của tài khoản này", "CUSTOMER_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses() {
        CustomerProfile customer = getCurrentCustomerProfile();
        return addressRepository.findByCustomerIdOrderByCreatedAtAsc(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddress(String id) {
        CustomerProfile customer = getCurrentCustomerProfile();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressException("Không tìm thấy địa chỉ này", "ADDRESS_NOT_FOUND", HttpStatus.NOT_FOUND));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new AddressException("Bạn không có quyền xem địa chỉ này", "ADDRESS_FORBIDDEN", HttpStatus.FORBIDDEN);
        }
        return mapToResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        CustomerProfile customer = getCurrentCustomerProfile();
        
        List<Address> existing = addressRepository.findByCustomerIdOrderByCreatedAtAsc(customer.getId());
        boolean isFirst = existing.isEmpty();
        boolean setAsDefault = isFirst || (request.getIsDefault() != null && request.getIsDefault());

        if (setAsDefault) {
            customerProfileRepository.findByIdWithLock(customer.getId());
            addressRepository.findByCustomerIdAndIsDefaultTrue(customer.getId())
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .id(UUID.randomUUID().toString())
                .customer(customer)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .addressDetail(request.getAddressDetail())
                .addressLabel(request.getAddressLabel())
                .isDefault(setAsDefault)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }

    @Transactional
    public AddressResponse updateAddress(String id, AddressRequest request) {
        CustomerProfile customer = getCurrentCustomerProfile();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressException("Không tìm thấy địa chỉ này", "ADDRESS_NOT_FOUND", HttpStatus.NOT_FOUND));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new AddressException("Bạn không có quyền sửa địa chỉ này", "ADDRESS_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        boolean requestDefault = request.getIsDefault() != null && request.getIsDefault();
        if (requestDefault && !address.getIsDefault()) {
            customerProfileRepository.findByIdWithLock(customer.getId());
            addressRepository.findByCustomerIdAndIsDefaultTrue(customer.getId())
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
            address.setIsDefault(true);
        } else if (!requestDefault && address.getIsDefault()) {
            List<Address> existing = addressRepository.findByCustomerIdOrderByCreatedAtAsc(customer.getId());
            if (existing.size() > 1) {
                customerProfileRepository.findByIdWithLock(customer.getId());
                address.setIsDefault(false);
                existing.stream()
                        .filter(a -> !a.getId().equals(address.getId()))
                        .findFirst()
                        .ifPresent(a -> {
                            a.setIsDefault(true);
                            addressRepository.save(a);
                        });
            } else {
                address.setIsDefault(true);
            }
        }

        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setAddressDetail(request.getAddressDetail());
        address.setAddressLabel(request.getAddressLabel());
        address.setUpdatedAt(LocalDateTime.now());

        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }

    @Transactional
    public AddressResponse setDefaultAddress(String id) {
        CustomerProfile customer = getCurrentCustomerProfile();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressException("Không tìm thấy địa chỉ này", "ADDRESS_NOT_FOUND", HttpStatus.NOT_FOUND));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new AddressException("Bạn không có quyền sửa địa chỉ này", "ADDRESS_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        if (!address.getIsDefault()) {
            customerProfileRepository.findByIdWithLock(customer.getId());
            addressRepository.findByCustomerIdAndIsDefaultTrue(customer.getId())
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
            address.setIsDefault(true);
            addressRepository.save(address);
        }

        return mapToResponse(address);
    }

    @Transactional
    public void deleteAddress(String id) {
        CustomerProfile customer = getCurrentCustomerProfile();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressException("Không tìm thấy địa chỉ này", "ADDRESS_NOT_FOUND", HttpStatus.NOT_FOUND));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new AddressException("Bạn không có quyền xóa địa chỉ này", "ADDRESS_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        if (address.getIsDefault()) {
            customerProfileRepository.findByIdWithLock(customer.getId());
            List<Address> remaining = addressRepository.findByCustomerIdOrderByCreatedAtAsc(customer.getId())
                    .stream()
                    .filter(a -> !a.getId().equals(id))
                    .collect(Collectors.toList());
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }

        addressRepository.delete(address);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .addressDetail(address.getAddressDetail())
                .addressLabel(address.getAddressLabel())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
