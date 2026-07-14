package com.phobo.management.category;

import com.phobo.management.category.dto.CategoryRequest;
import com.phobo.management.category.dto.CategoryResponse;
import com.phobo.management.category.service.CategoryService;
import com.phobo.management.entity.Category;
import com.phobo.management.exception.BadRequestException;
import com.phobo.management.exception.ResourceNotFoundException;
import com.phobo.management.repository.CategoryRepository;
import com.phobo.management.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest request;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id("cat-123")
                .categoryName("Phở nước")
                .description("Phở ngon")
                .displayOrder(1)
                .isActive(true)
                .build();

        request = CategoryRequest.builder()
                .categoryName("Phở nước")
                .description("Phở ngon")
                .displayOrder(1)
                .isActive(true)
                .build();
    }

    @Test
    void getCategoryById_success() {
        when(categoryRepository.findByIdAndDeletedAtIsNull("cat-123")).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategoryById("cat-123");

        assertNotNull(response);
        assertEquals("Phở nước", response.getCategoryName());
        assertEquals("cat-123", response.getId());
    }

    @Test
    void getCategoryById_notFound() {
        when(categoryRepository.findByIdAndDeletedAtIsNull("cat-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById("cat-123"));
    }

    @Test
    void createCategory_success() {
        when(categoryRepository.existsByCategoryNameIgnoreCaseAndDeletedAtIsNull("Phở nước")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Phở nước", response.getCategoryName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_duplicateName_throwsBadRequest() {
        when(categoryRepository.existsByCategoryNameIgnoreCaseAndDeletedAtIsNull("Phở nước")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_hasLinkedProducts_throwsBadRequest() {
        when(categoryRepository.findByIdAndDeletedAtIsNull("cat-123")).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryIdAndDeletedAtIsNull("cat-123")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("cat-123"));
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
