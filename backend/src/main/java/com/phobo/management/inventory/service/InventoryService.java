package com.phobo.management.inventory.service;

import com.phobo.management.audit.service.AuditLogService;
import com.phobo.management.exception.AppException;
import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.entity.Ingredient;
import com.phobo.management.entity.InventoryTransaction;
import com.phobo.management.inventory.dto.*;
import com.phobo.management.repository.EmployeeProfileRepository;
import com.phobo.management.repository.IngredientRepository;
import com.phobo.management.repository.InventoryTransactionRepository;
import com.phobo.management.security.EmployeeAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final IngredientRepository ingredientRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final EmployeeProfileRepository employeeRepository;
    private final EmployeeAuthorizationService employeeAuthService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<IngredientResponse> getIngredients(String keyword, Boolean isActive, Pageable pageable) {
        return ingredientRepository.findIngredientsWithFilters(keyword, isActive, pageable)
                .map(this::mapToIngredientResponse);
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> getLowStockIngredients() {
        return ingredientRepository.findLowStockIngredients().stream()
                .map(this::mapToIngredientResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IngredientResponse createIngredient(IngredientRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        String code = request.getIngredientCode().trim().toUpperCase();

        if (ingredientRepository.existsByIngredientCode(code)) {
            throw new AppException("Mã nguyên liệu đã tồn tại", "INGREDIENT_CODE_EXISTS");
        }

        BigDecimal initialStock = request.getInitialStock() != null ? request.getInitialStock() : BigDecimal.ZERO;

        Ingredient ingredient = Ingredient.builder()
                .id(UUID.randomUUID().toString())
                .ingredientCode(code)
                .name(request.getName().trim())
                .unit(request.getUnit().trim())
                .minThreshold(request.getMinThreshold())
                .currentStock(initialStock)
                .isActive(true)
                .build();

        ingredientRepository.save(ingredient);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "CREATE_INGREDIENT", "Ingredient",
                ingredient.getId(), "SUCCESS", "Tạo nguyên liệu mới: " + ingredient.getName(), null, null);

        return mapToIngredientResponse(ingredient);
    }

    @Transactional
    public IngredientResponse updateIngredient(String id, IngredientRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        Ingredient ingredient = ingredientRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException("Không tìm thấy nguyên liệu", "INGREDIENT_NOT_FOUND"));

        String code = request.getIngredientCode().trim().toUpperCase();
        if (!ingredient.getIngredientCode().equalsIgnoreCase(code) && ingredientRepository.existsByIngredientCode(code)) {
            throw new AppException("Mã nguyên liệu đã tồn tại", "INGREDIENT_CODE_EXISTS");
        }

        ingredient.setIngredientCode(code);
        ingredient.setName(request.getName().trim());
        ingredient.setUnit(request.getUnit().trim());
        ingredient.setMinThreshold(request.getMinThreshold());

        ingredientRepository.save(ingredient);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "UPDATE_INGREDIENT", "Ingredient",
                ingredient.getId(), "SUCCESS", "Cập nhật nguyên liệu: " + ingredient.getName(), null, null);

        return mapToIngredientResponse(ingredient);
    }

    @Transactional
    public InventoryTransactionResponse processStockAdjustment(InventoryStockAdjustmentRequest request) {
        String currentUserId = employeeAuthService.getCurrentUserId();
        EmployeeProfile performer = employeeRepository.findByUserId(currentUserId).orElse(null);

        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Số lượng phải lớn hơn 0", "INVALID_INVENTORY_QUANTITY");
        }

        // Lock ingredient with PESSIMISTIC_WRITE
        Ingredient ingredient = ingredientRepository.findByIdForUpdate(request.getIngredientId())
                .orElseThrow(() -> new AppException("Không tìm thấy nguyên liệu", "INGREDIENT_NOT_FOUND"));

        BigDecimal stockBefore = ingredient.getCurrentStock();
        BigDecimal stockAfter;
        String type = request.getTransactionType().trim().toUpperCase();

        if ("IMPORT".equals(type) || "ADJUST_IN".equals(type)) {
            stockAfter = stockBefore.add(request.getQuantity());
        } else if ("EXPORT".equals(type) || "ADJUST_OUT".equals(type) || "WASTE".equals(type)) {
            if (stockBefore.compareTo(request.getQuantity()) < 0) {
                throw new AppException("Số lượng tồn kho không đủ để xuất", "INSUFFICIENT_STOCK");
            }
            stockAfter = stockBefore.subtract(request.getQuantity());
        } else {
            throw new AppException("Loại giao dịch kho không hợp lệ", "INVALID_TRANSACTION_TYPE");
        }

        ingredient.setCurrentStock(stockAfter);
        ingredientRepository.save(ingredient);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .id(UUID.randomUUID().toString())
                .ingredient(ingredient)
                .transactionType(type)
                .quantity(request.getQuantity())
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .performedBy(performer)
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        auditLogService.logAction(currentUserId, "ROLE_MANAGER", "ADJUST_STOCK_" + type, "InventoryTransaction",
                transaction.getId(), "SUCCESS", "Giao dịch " + type + " nguyên liệu " + ingredient.getName() + ": " + request.getQuantity() + " " + ingredient.getUnit(), null, null);

        return mapToTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> getTransactions(String ingredientId, String transactionType, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return transactionRepository.findTransactionsWithFilters(ingredientId, transactionType, from, to, pageable)
                .map(this::mapToTransactionResponse);
    }

    private IngredientResponse mapToIngredientResponse(Ingredient ing) {
        boolean isLowStock = ing.getCurrentStock().compareTo(ing.getMinThreshold()) <= 0;
        return IngredientResponse.builder()
                .id(ing.getId())
                .ingredientCode(ing.getIngredientCode())
                .name(ing.getName())
                .unit(ing.getUnit())
                .minThreshold(ing.getMinThreshold())
                .currentStock(ing.getCurrentStock())
                .isLowStock(isLowStock)
                .isActive(ing.getIsActive())
                .build();
    }

    private InventoryTransactionResponse mapToTransactionResponse(InventoryTransaction it) {
        Ingredient ing = it.getIngredient();
        return InventoryTransactionResponse.builder()
                .id(it.getId())
                .ingredientId(ing.getId())
                .ingredientName(ing.getName())
                .ingredientCode(ing.getIngredientCode())
                .unit(ing.getUnit())
                .transactionType(it.getTransactionType())
                .quantity(it.getQuantity())
                .stockBefore(it.getStockBefore())
                .stockAfter(it.getStockAfter())
                .performedByEmployeeName(it.getPerformedBy() != null ? it.getPerformedBy().getFullName() : "SYSTEM")
                .note(it.getNote())
                .createdAt(it.getCreatedAt())
                .build();
    }
}
