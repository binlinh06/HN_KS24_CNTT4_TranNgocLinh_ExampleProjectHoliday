package com.phobo.management.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phobo.management.cart.dto.*;
import com.phobo.management.entity.*;
import com.phobo.management.exception.BadRequestException;
import com.phobo.management.exception.CartException;
import com.phobo.management.exception.ResourceNotFoundException;
import com.phobo.management.repository.*;
import com.phobo.management.security.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final VoucherRepository voucherRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final CartPricingService cartPricingService;
    private final CartValidationService cartValidationService;
    private final VoucherValidationService voucherValidationService;
    private final ObjectMapper objectMapper;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            CustomerProfileRepository customerProfileRepository,
            ProductRepository productRepository,
            ProductOptionRepository productOptionRepository,
            VoucherRepository voucherRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            CartPricingService cartPricingService,
            CartValidationService cartValidationService,
            VoucherValidationService voucherValidationService,
            ObjectMapper objectMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.productRepository = productRepository;
        this.productOptionRepository = productOptionRepository;
        this.voucherRepository = voucherRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.cartPricingService = cartPricingService;
        this.cartValidationService = cartValidationService;
        this.voucherValidationService = voucherValidationService;
        this.objectMapper = objectMapper;
    }

    public String getInfo() {
        return "Cart Service Active";
    }

    private CustomerProfile getCurrentCustomerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CartException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new CartException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException("Không tìm thấy hồ sơ khách hàng của tài khoản này", "CUSTOMER_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    @Transactional
    public Cart getOrCreateCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    CustomerProfile customer = customerProfileRepository.findById(customerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
                    Cart newCart = Cart.builder()
                            .id(UUID.randomUUID().toString())
                            .customer(customer)
                            .items(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        CustomerProfile customer = getCurrentCustomerProfile();
        Cart cart = getOrCreateCart(customer.getId());
        return mapToResponse(cart);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public CartResponse addItem(CartItemRequest request) {
        CustomerProfile customer = getCurrentCustomerProfile();
        
        // Use Pessimistic write lock to prevent concurrent modifications
        Cart cart = cartRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseGet(() -> getOrCreateCart(customer.getId()));

        Product product = productRepository.findById(request.getProductId().toString())
                .orElseThrow(() -> new CartException("Không tìm thấy món ăn", "PRODUCT_NOT_AVAILABLE", HttpStatus.BAD_REQUEST));

        // Validate optionIds contain no duplicates or nulls
        List<UUID> optionIds = request.getOptionIds() != null ? request.getOptionIds() : new ArrayList<>();
        if (optionIds.stream().anyMatch(Objects::isNull)) {
            throw new CartException("Danh sách tùy chọn chứa phần tử không hợp lệ", "INVALID_PRODUCT_OPTION", HttpStatus.BAD_REQUEST);
        }
        if (optionIds.stream().distinct().count() != optionIds.size()) {
            throw new CartException("Danh sách tùy chọn không được chứa phần tử trùng lặp", "INVALID_PRODUCT_OPTION", HttpStatus.BAD_REQUEST);
        }

        // Fetch options
        List<ProductOption> selectedOptions = productOptionRepository.findAllById(
                optionIds.stream().map(UUID::toString).collect(Collectors.toList())
        );

        // Service validation
        List<String> validationErrors = cartValidationService.validateProductConfiguration(product, selectedOptions);
        if (!validationErrors.isEmpty()) {
            throw new CartException(validationErrors.get(0), "INVALID_PRODUCT_OPTION", HttpStatus.BAD_REQUEST);
        }

        String specialNote = request.getSpecialNote() != null ? request.getSpecialNote().trim() : null;
        if (specialNote != null && specialNote.length() > 150) {
            throw new CartException("Ghi chú không được vượt quá 150 ký tự", "INVALID_QUANTITY", HttpStatus.BAD_REQUEST);
        }
        if (specialNote != null && specialNote.isEmpty()) {
            specialNote = null;
        }

        // Check if configuration matches an existing item
        CartItem matchedItem = null;
        for (CartItem item : cart.getItems()) {
            if (isSameConfiguration(item, product.getId(), selectedOptions, specialNote)) {
                matchedItem = item;
                break;
            }
        }

        if (matchedItem != null) {
            int newQuantity = matchedItem.getQuantity() + request.getQuantity();
            if (newQuantity > 99) {
                throw new CartException("Số lượng trong giỏ hàng vượt quá giới hạn tối đa (99 món)", "INVALID_QUANTITY", HttpStatus.BAD_REQUEST);
            }
            matchedItem.setQuantity(newQuantity);
            // Do NOT update unitPriceSnapshot when only quantity changes
        } else {
            if (request.getQuantity() < 1 || request.getQuantity() > 99) {
                throw new CartException("Số lượng món phải từ 1 đến 99", "INVALID_QUANTITY", HttpStatus.BAD_REQUEST);
            }
            CartItem newItem = CartItem.builder()
                    .id(UUID.randomUUID().toString())
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .options(new HashSet<>(selectedOptions))
                    .specialNote(specialNote)
                    .build();
            
            // Set snapshot on creation
            BigDecimal unitPrice = cartPricingService.calculateItemUnitPrice(newItem);
            newItem.setUnitPriceSnapshot(unitPrice);
            
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        // Revalidate voucher
        revalidateVoucher(cart);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(String cartItemId, CartItemUpdateRequest request) {
        CustomerProfile customer = getCurrentCustomerProfile();
        Cart cart = cartRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // Verify ownership
        if (!itemToUpdate.getCart().getId().equals(cart.getId())) {
            throw new CartException("Bạn không có quyền chỉnh sửa sản phẩm này", "CART_ITEM_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        boolean configChanged = false;

        // Apply PATCH semantics
        // 1. quantity
        if (request.getQuantity() != null) {
            if (request.getQuantity() < 1 || request.getQuantity() > 99) {
                throw new CartException("Số lượng món phải từ 1 đến 99", "INVALID_QUANTITY", HttpStatus.BAD_REQUEST);
            }
            itemToUpdate.setQuantity(request.getQuantity());
        }

        // 2. specialNote
        if (request.getSpecialNote() != null) {
            String note = request.getSpecialNote().trim();
            if (note.length() > 150) {
                throw new CartException("Ghi chú không được vượt quá 150 ký tự", "INVALID_QUANTITY", HttpStatus.BAD_REQUEST);
            }
            if (note.isEmpty()) {
                itemToUpdate.setSpecialNote(null);
            } else {
                itemToUpdate.setSpecialNote(note);
            }
        }

        // 3. optionIds (configuration)
        if (request.getOptionIds() != null) {
            configChanged = true;
            List<UUID> optionIds = request.getOptionIds();
            if (optionIds.stream().anyMatch(Objects::isNull)) {
                throw new CartException("Danh sách tùy chọn chứa phần tử không hợp lệ", "INVALID_PRODUCT_OPTION", HttpStatus.BAD_REQUEST);
            }
            if (optionIds.stream().distinct().count() != optionIds.size()) {
                throw new CartException("Danh sách tùy chọn không được chứa phần tử trùng lặp", "INVALID_PRODUCT_OPTION", HttpStatus.BAD_REQUEST);
            }

            List<ProductOption> selectedOptions = productOptionRepository.findAllById(
                    optionIds.stream().map(UUID::toString).collect(Collectors.toList())
            );

            // Validate configuration
            List<String> validationErrors = cartValidationService.validateProductConfiguration(itemToUpdate.getProduct(), selectedOptions);
            if (!validationErrors.isEmpty()) {
                throw new CartException(validationErrors.get(0), "INVALID_PRODUCT_OPTION", HttpStatus.BAD_REQUEST);
            }

            itemToUpdate.setOptions(new HashSet<>(selectedOptions));

            // Update snapshot when configuration changes
            BigDecimal unitPrice = cartPricingService.calculateItemUnitPrice(itemToUpdate);
            itemToUpdate.setUnitPriceSnapshot(unitPrice);
        }

        // Check if the update makes this item identical to another item in the cart
        List<CartItem> duplicates = cart.getItems().stream()
                .filter(item -> !item.getId().equals(itemToUpdate.getId()) &&
                        isSameConfiguration(item, itemToUpdate.getProduct().getId(), itemToUpdate.getOptions(), itemToUpdate.getSpecialNote()))
                .collect(Collectors.toList());

        if (!duplicates.isEmpty()) {
            CartItem firstDuplicate = duplicates.get(0);
            int newQuantity = firstDuplicate.getQuantity() + itemToUpdate.getQuantity();
            if (newQuantity > 99) {
                newQuantity = 99; // Cap at 99
            }
            firstDuplicate.setQuantity(newQuantity);
            // Remove the current item because it was merged into the duplicate
            cart.getItems().remove(itemToUpdate);
            cartItemRepository.delete(itemToUpdate);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        revalidateVoucher(cart);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(String cartItemId) {
        CustomerProfile customer = getCurrentCustomerProfile();
        Cart cart = cartRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!itemToRemove.getCart().getId().equals(cart.getId())) {
            throw new CartException("Bạn không có quyền xóa sản phẩm này", "CART_ITEM_FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        cart.getItems().remove(itemToRemove);
        cartItemRepository.delete(itemToRemove);

        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        revalidateVoucher(cart);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse clearCart() {
        CustomerProfile customer = getCurrentCustomerProfile();
        Cart cart = cartRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().clear();
        cart.setAppliedVoucher(null);
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse applyVoucher(String code) {
        CustomerProfile customer = getCurrentCustomerProfile();
        Cart cart = cartRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndDeletedAtIsNull(code)
                .orElseThrow(() -> new CartException("Mã giảm giá không tồn tại.", "VOUCHER_NOT_FOUND", HttpStatus.BAD_REQUEST));

        // Compute subtotal of valid items
        List<CartItem> validItems = cart.getItems().stream()
                .filter(item -> cartValidationService.validateProductConfiguration(item.getProduct(), item.getOptions()).isEmpty())
                .collect(Collectors.toList());

        BigDecimal subtotal = cartPricingService.calculateSubtotal(validItems);

        List<String> errors = voucherValidationService.validateVoucher(voucher, subtotal);
        if (!errors.isEmpty()) {
            throw new CartException(errors.get(0), "INVALID_VOUCHER", HttpStatus.BAD_REQUEST);
        }

        cart.setAppliedVoucher(voucher);
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse removeVoucher() {
        CustomerProfile customer = getCurrentCustomerProfile();
        Cart cart = cartRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.setAppliedVoucher(null);
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public CartMergeResponse mergeCart(List<GuestCartMergeItemRequest> mergeItems, String idempotencyKey) {
        CustomerProfile customer = getCurrentCustomerProfile();

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new CartException("Thiếu idempotency key", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }
        
        // Validate Idempotency-Key is UUID
        try {
            UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new CartException("Idempotency key không hợp lệ", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // Step 1: Create request hash
        String requestHash = calculateHash(mergeItems);

        // Claim key using Native INSERT IGNORE
        int affectedRows = idempotencyRecordRepository.claimIdempotencyKey(
                UUID.randomUUID().toString(),
                customer.getId(),
                idempotencyKey,
                "MERGE_CART",
                requestHash,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24) // Expires in 24 hours
        );

        if (affectedRows == 0) {
            // Key already claimed, read the record
            IdempotencyRecord record = idempotencyRecordRepository.findByCustomerIdAndOperationAndIdempotencyKey(
                    customer.getId(), "MERGE_CART", idempotencyKey
            ).orElseThrow(() -> new CartException("Lỗi hệ thống ghi nhận idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

            if (!record.getRequestHash().equals(requestHash)) {
                throw new CartException("Idempotency key đã được sử dụng cho một yêu cầu khác", "IDEMPOTENCY_KEY_REUSED", HttpStatus.BAD_REQUEST);
            }

            if ("COMPLETED".equals(record.getStatus())) {
                try {
                    return objectMapper.readValue(record.getResponseBody(), CartMergeResponse.class);
                } catch (Exception e) {
                    throw new CartException("Lỗi đọc dữ liệu cache idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                // Check if expired
                if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
                    // Stale/expired record, let's delete it and retry
                    idempotencyRecordRepository.delete(record);
                    // Re-run merge (recursion is safe since record is deleted)
                    return mergeCart(mergeItems, idempotencyKey);
                } else {
                    throw new CartException("Yêu cầu đồng bộ giỏ hàng đang được xử lý", "IDEMPOTENCY_REQUEST_IN_PROGRESS", HttpStatus.CONFLICT);
                }
            }
        }

        // Lock Cart
        Cart cart = cartRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseGet(() -> getOrCreateCart(customer.getId()));

        List<UUID> acceptedItems = new ArrayList<>();
        List<CartMergeResponse.RejectedItem> rejectedItems = new ArrayList<>();

        for (GuestCartMergeItemRequest itemRequest : mergeItems) {
            try {
                // Find product
                Product product = productRepository.findById(itemRequest.getProductId().toString())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn"));

                // Validate options duplicate/nulls
                List<UUID> optionIds = itemRequest.getOptionIds() != null ? itemRequest.getOptionIds() : new ArrayList<>();
                if (optionIds.stream().anyMatch(Objects::isNull) || optionIds.stream().distinct().count() != optionIds.size()) {
                    throw new BadRequestException("Tùy chọn không hợp lệ hoặc bị trùng");
                }

                List<ProductOption> selectedOptions = productOptionRepository.findAllById(
                        optionIds.stream().map(UUID::toString).collect(Collectors.toList())
                );

                // Service validation
                List<String> validationErrors = cartValidationService.validateProductConfiguration(product, selectedOptions);
                if (!validationErrors.isEmpty()) {
                    throw new BadRequestException(validationErrors.get(0));
                }

                String specialNote = itemRequest.getSpecialNote() != null ? itemRequest.getSpecialNote().trim() : null;
                if (specialNote != null && specialNote.length() > 150) {
                    throw new BadRequestException("Ghi chú quá dài");
                }
                if (specialNote != null && specialNote.isEmpty()) {
                    specialNote = null;
                }

                // Check duplicate in cart
                CartItem matchedItem = null;
                for (CartItem item : cart.getItems()) {
                    if (isSameConfiguration(item, product.getId(), selectedOptions, specialNote)) {
                        matchedItem = item;
                        break;
                    }
                }

                if (matchedItem != null) {
                    int newQuantity = matchedItem.getQuantity() + itemRequest.getQuantity();
                    if (newQuantity > 99) {
                        newQuantity = 99;
                    }
                    matchedItem.setQuantity(newQuantity);
                } else {
                    CartItem newItem = CartItem.builder()
                            .id(UUID.randomUUID().toString())
                            .cart(cart)
                            .product(product)
                            .quantity(itemRequest.getQuantity())
                            .options(new HashSet<>(selectedOptions))
                            .specialNote(specialNote)
                            .build();

                    // Set unitPriceSnapshot
                    BigDecimal unitPrice = cartPricingService.calculateItemUnitPrice(newItem);
                    newItem.setUnitPriceSnapshot(unitPrice);

                    cart.getItems().add(newItem);
                }

                acceptedItems.add(itemRequest.getClientItemId());

            } catch (Exception e) {
                rejectedItems.add(CartMergeResponse.RejectedItem.builder()
                        .clientItemId(itemRequest.getClientItemId())
                        .errorCode(e instanceof CartException ? ((CartException) e).getErrorCode() : "INVALID_ITEM")
                        .message(e.getMessage())
                        .build());
            }
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        revalidateVoucher(cart);

        CartResponse cartResponse = mapToResponse(cart);
        CartMergeResponse mergeResponse = CartMergeResponse.builder()
                .acceptedItems(acceptedItems)
                .rejectedItems(rejectedItems)
                .cart(cartResponse)
                .build();

        // Update IdempotencyRecord to COMPLETED
        IdempotencyRecord record = idempotencyRecordRepository.findByCustomerIdAndOperationAndIdempotencyKey(
                customer.getId(), "MERGE_CART", idempotencyKey
        ).orElseThrow(() -> new CartException("Lỗi hệ thống ghi nhận idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            record.setResponseBody(objectMapper.writeValueAsString(mergeResponse));
            record.setStatus("COMPLETED");
            idempotencyRecordRepository.save(record);
        } catch (Exception e) {
            throw new CartException("Lỗi lưu cache kết quả idempotency", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return mergeResponse;
    }

    private boolean isSameConfiguration(CartItem item, String productId, Collection<ProductOption> options, String specialNote) {
        if (!item.getProduct().getId().equals(productId)) {
            return false;
        }

        String note1 = item.getSpecialNote() != null ? item.getSpecialNote().trim() : "";
        String note2 = specialNote != null ? specialNote.trim() : "";
        if (!note1.equalsIgnoreCase(note2)) {
            return false;
        }

        Set<String> set1 = item.getOptions().stream().map(ProductOption::getId).collect(Collectors.toSet());
        Set<String> set2 = options.stream().map(ProductOption::getId).collect(Collectors.toSet());
        return set1.equals(set2);
    }

    private String calculateHash(List<GuestCartMergeItemRequest> items) {
        if (items == null) return "";
        List<GuestCartMergeItemRequest> sortedItems = new ArrayList<>(items);
        sortedItems.sort(Comparator.comparing(i -> i.getClientItemId().toString()));
        
        StringBuilder sb = new StringBuilder();
        for (GuestCartMergeItemRequest item : sortedItems) {
            sb.append(item.getClientItemId()).append("|");
            sb.append(item.getProductId()).append("|");
            sb.append(item.getQuantity()).append("|");
            
            String note = item.getSpecialNote() != null ? item.getSpecialNote().trim().toLowerCase() : "";
            sb.append(note).append("|");
            
            List<UUID> options = item.getOptionIds() != null ? new ArrayList<>(item.getOptionIds()) : new ArrayList<>();
            options.sort(Comparator.comparing(UUID::toString));
            for (UUID opt : options) {
                sb.append(opt).append(",");
            }
            sb.append(";");
        }
        
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private void revalidateVoucher(Cart cart) {
        if (cart.getAppliedVoucher() == null) {
            return;
        }

        // Subtotal of valid items
        List<CartItem> validItems = cart.getItems().stream()
                .filter(item -> cartValidationService.validateProductConfiguration(item.getProduct(), item.getOptions()).isEmpty())
                .collect(Collectors.toList());

        BigDecimal subtotal = cartPricingService.calculateSubtotal(validItems);

        List<String> errors = voucherValidationService.validateVoucher(cart.getAppliedVoucher(), subtotal);
        if (!errors.isEmpty()) {
            cart.setAppliedVoucher(null);
            cartRepository.save(cart);
        }
    }

    public CartResponse mapToResponse(Cart cart) {
        String voucherRemovalReason = null;
        Voucher appliedVoucher = cart.getAppliedVoucher();

        // Map items
        List<CartItemResponse> itemResponses = new ArrayList<>();
        boolean isCartValid = true;

        for (CartItem item : cart.getItems()) {
            // Validation
            List<String> validationErrors = cartValidationService.validateProductConfiguration(item.getProduct(), item.getOptions());
            boolean isValid = validationErrors.isEmpty();
            if (!isValid) {
                isCartValid = false;
            }

            // Calculation
            BigDecimal basePrice = item.getProduct().getBasePrice();
            BigDecimal optionsPrice = cartPricingService.calculateOptionsPrice(item.getOptions());
            BigDecimal currentUnitPrice = cartPricingService.calculateItemUnitPrice(item);
            BigDecimal lineTotal = cartPricingService.calculateLineTotal(item);

            BigDecimal snapshot = item.getUnitPriceSnapshot();
            boolean priceChanged = snapshot != null && snapshot.compareTo(currentUnitPrice) != 0;

            List<CartOptionResponse> options = item.getOptions().stream()
                    .map(o -> CartOptionResponse.builder()
                            .id(UUID.fromString(o.getId()))
                            .groupName(o.getGroup().getGroupName())
                            .optionName(o.getOptionName())
                            .incrementalPrice(o.getIncrementalPrice())
                            .build())
                    .sorted(Comparator.comparing(CartOptionResponse::getGroupName).thenComparing(CartOptionResponse::getOptionName))
                    .collect(Collectors.toList());

            CartItemResponse.CartProductResponse product = CartItemResponse.CartProductResponse.builder()
                    .id(UUID.fromString(item.getProduct().getId()))
                    .name(item.getProduct().getProductName())
                    .slug(item.getProduct().getSlug())
                    .imageUrl(item.getProduct().getImageUrl())
                    .available(Boolean.TRUE.equals(item.getProduct().getIsAvailable()) && item.getProduct().getDeletedAt() == null)
                    .build();

            itemResponses.add(CartItemResponse.builder()
                    .id(UUID.fromString(item.getId()))
                    .product(product)
                    .selectedOptions(options)
                    .quantity(item.getQuantity())
                    .specialNote(item.getSpecialNote())
                    .basePrice(basePrice)
                    .optionsPrice(optionsPrice)
                    .unitPriceSnapshot(snapshot)
                    .currentUnitPrice(currentUnitPrice)
                    .lineTotal(lineTotal)
                    .priceChanged(priceChanged)
                    .valid(isValid)
                    .validationMessages(validationErrors)
                    .build());
        }

        // Calculate totals based on valid items only
        List<CartItem> validItems = cart.getItems().stream()
                .filter(item -> cartValidationService.validateProductConfiguration(item.getProduct(), item.getOptions()).isEmpty())
                .collect(Collectors.toList());

        BigDecimal subtotal = cartPricingService.calculateSubtotal(validItems);

        BigDecimal discountAmount = BigDecimal.ZERO;
        CartResponse.CartVoucherResponse voucherResp = null;

        if (appliedVoucher != null) {
            List<String> voucherErrors = voucherValidationService.validateVoucher(appliedVoucher, subtotal);
            if (!voucherErrors.isEmpty()) {
                // Auto-remove voucher on response generation
                voucherRemovalReason = voucherErrors.get(0);
                cart.setAppliedVoucher(null);
                cartRepository.save(cart);
                appliedVoucher = null;
            } else {
                discountAmount = voucherValidationService.calculateDiscount(appliedVoucher, subtotal);
                voucherResp = CartResponse.CartVoucherResponse.builder()
                        .id(UUID.fromString(appliedVoucher.getId()))
                        .code(appliedVoucher.getCode())
                        .discountType(appliedVoucher.getDiscountType())
                        .discountValue(appliedVoucher.getDiscountValue())
                        .minOrderValue(appliedVoucher.getMinOrderValue())
                        .build();
            }
        }

        BigDecimal estimatedTotal = cartPricingService.calculateEstimatedTotal(subtotal, discountAmount);

        int totalQty = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();

        List<String> cartValidationMessages = new ArrayList<>();
        if (!isCartValid) {
            cartValidationMessages.add("Giỏ hàng chứa sản phẩm hoặc tùy chọn không hợp lệ. Vui lòng kiểm tra lại.");
        }

        return CartResponse.builder()
                .id(UUID.fromString(cart.getId()))
                .items(itemResponses)
                .itemCount(cart.getItems().size())
                .totalQuantity(totalQty)
                .subtotal(subtotal)
                .voucher(voucherResp)
                .discountAmount(discountAmount)
                .estimatedTotal(estimatedTotal)
                .valid(isCartValid)
                .validationMessages(cartValidationMessages)
                .voucherRemovalReason(voucherRemovalReason)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
