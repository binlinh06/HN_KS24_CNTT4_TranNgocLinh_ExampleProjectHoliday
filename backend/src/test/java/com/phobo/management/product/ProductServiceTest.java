package com.phobo.management.product;

import com.phobo.management.entity.Category;
import com.phobo.management.entity.Product;
import com.phobo.management.exception.BadRequestException;
import com.phobo.management.exception.ResourceNotFoundException;
import com.phobo.management.product.dto.ProductRequest;
import com.phobo.management.product.dto.ProductResponse;
import com.phobo.management.product.service.ProductService;
import com.phobo.management.repository.CategoryRepository;
import com.phobo.management.repository.OrderItemRepository;
import com.phobo.management.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id("cat-123")
                .categoryName("Phở nước")
                .isActive(true)
                .build();

        product = Product.builder()
                .id("prod-123")
                .productName("Phở chín")
                .slug("pho-chin")
                .category(category)
                .basePrice(BigDecimal.valueOf(50000.0))
                .isAvailable(true)
                .isFeatured(false)
                .preparationTimeMinutes(15)
                .build();

        request = ProductRequest.builder()
                .productName("Phở chín")
                .categoryId("cat-123")
                .basePrice(BigDecimal.valueOf(50000.0))
                .isAvailable(true)
                .isFeatured(false)
                .preparationTimeMinutes(15)
                .build();
    }

    @Test
    void createProduct_success() {
        when(categoryRepository.findByIdAndDeletedAtIsNull("cat-123")).thenReturn(Optional.of(category));
        when(productRepository.existsByProductNameIgnoreCaseAndDeletedAtIsNull("Phở chín")).thenReturn(false);
        when(productRepository.existsBySlugAndDeletedAtIsNull("pho-chin")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("Phở chín", response.getProductName());
        assertEquals("pho-chin", response.getSlug());
    }

    @Test
    void createProduct_invalidCategory_throwsBadRequest() {
        when(categoryRepository.findByIdAndDeletedAtIsNull("cat-123")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> productService.createProduct(request));
    }

    @Test
    void deleteProduct_linkedInOrders_throwsBadRequest() {
        when(productRepository.findByIdAndDeletedAtIsNull("prod-123")).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId("prod-123")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> productService.deleteProduct("prod-123"));
    }
}
