package com.phobo.management.category.service;

import com.phobo.management.category.dto.CategoryRequest;
import com.phobo.management.category.dto.CategoryResponse;
import com.phobo.management.entity.Category;
import com.phobo.management.exception.BadRequestException;
import com.phobo.management.exception.ResourceNotFoundException;
import com.phobo.management.repository.CategoryRepository;
import com.phobo.management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getPublicCategories() {
        return categoryRepository.findAllByDeletedAtIsNullAndIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByDeletedAtIsNullOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String normalizedName = request.getCategoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndDeletedAtIsNull(normalizedName)) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        Category category = Category.builder()
                .id(UUID.randomUUID().toString())
                .categoryName(normalizedName)
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        String normalizedName = request.getCategoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndDeletedAtIsNullAndIdNot(normalizedName, id)) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        category.setCategoryName(normalizedName);
        category.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Transactional
    public CategoryResponse toggleStatus(String id, Boolean isActive) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        
        category.setIsActive(isActive);
        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        if (productRepository.existsByCategoryIdAndDeletedAtIsNull(id)) {
            throw new BadRequestException("Không thể xóa dữ liệu vì đang được liên kết với dữ liệu nghiệp vụ khác");
        }

        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
