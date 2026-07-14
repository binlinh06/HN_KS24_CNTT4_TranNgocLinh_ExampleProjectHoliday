package com.phobo.management.product.service;

import com.phobo.management.entity.Category;
import com.phobo.management.entity.Product;
import com.phobo.management.exception.BadRequestException;
import com.phobo.management.exception.ResourceNotFoundException;
import com.phobo.management.product.dto.ProductRequest;
import com.phobo.management.product.dto.ProductResponse;
import com.phobo.management.repository.CategoryRepository;
import com.phobo.management.repository.OrderItemRepository;
import com.phobo.management.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String categoryId,
            String keyword,
            Boolean isAvailable,
            Boolean isFeatured,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortStr,
            int page,
            int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortStr != null) {
            switch (sortStr) {
                case "priceAsc":
                    sort = Sort.by(Sort.Direction.ASC, "basePrice");
                    break;
                case "priceDesc":
                    sort = Sort.by(Sort.Direction.DESC, "basePrice");
                    break;
                case "nameAsc":
                    sort = Sort.by(Sort.Direction.ASC, "productName");
                    break;
                case "nameDesc":
                    sort = Sort.by(Sort.Direction.DESC, "productName");
                    break;
                case "newest":
                default:
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out soft-deleted products
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (categoryId != null && !categoryId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("productName")), searchPattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(nameLike, descLike));
            }

            if (isAvailable != null) {
                predicates.add(cb.equal(root.get("isAvailable"), isAvailable));
            }

            if (isFeatured != null) {
                predicates.add(cb.equal(root.get("isFeatured"), isFeatured));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findAllByDeletedAtIsNullAndIsAvailableTrueAndIsFeaturedTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Danh mục không hợp lệ hoặc không tồn tại"));

        String normalizedName = request.getProductName().trim();
        if (productRepository.existsByProductNameIgnoreCaseAndDeletedAtIsNull(normalizedName)) {
            throw new BadRequestException("Tên sản phẩm đã tồn tại");
        }

        String slug = request.getSlug() != null && !request.getSlug().trim().isEmpty()
                ? request.getSlug().trim().toLowerCase()
                : slugify(normalizedName);

        if (productRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            // Append random suffix if slug conflicts
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 5);
        }

        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .productName(normalizedName)
                .slug(slug)
                .category(category)
                .basePrice(request.getBasePrice())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .preparationTimeMinutes(request.getPreparationTimeMinutes() != null ? request.getPreparationTimeMinutes() : 15)
                .build();

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Danh mục không hợp lệ hoặc không tồn tại"));

        String normalizedName = request.getProductName().trim();
        if (productRepository.existsByProductNameIgnoreCaseAndDeletedAtIsNullAndIdNot(normalizedName, id)) {
            throw new BadRequestException("Tên sản phẩm đã tồn tại");
        }

        String slug = request.getSlug() != null && !request.getSlug().trim().isEmpty()
                ? request.getSlug().trim().toLowerCase()
                : slugify(normalizedName);

        if (productRepository.existsBySlugAndDeletedAtIsNullAndIdNot(slug, id)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 5);
        }

        product.setProductName(normalizedName);
        product.setSlug(slug);
        product.setCategory(category);
        product.setBasePrice(request.getBasePrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        
        if (request.getIsAvailable() != null) {
            product.setIsAvailable(request.getIsAvailable());
        }
        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }
        if (request.getPreparationTimeMinutes() != null) {
            product.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        }

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Transactional
    public ProductResponse toggleAvailability(String id, Boolean isAvailable) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        product.setIsAvailable(isAvailable);
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Transactional
    public ProductResponse toggleFeatured(String id, Boolean isFeatured) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        product.setIsFeatured(isFeatured);
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        if (orderItemRepository.existsByProductId(id)) {
            throw new BadRequestException("Không thể xóa dữ liệu vì đang được liên kết với dữ liệu nghiệp vụ khác");
        }

        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .slug(product.getSlug())
                .basePrice(product.getBasePrice())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .isAvailable(product.getIsAvailable())
                .isFeatured(product.getIsFeatured())
                .preparationTimeMinutes(product.getPreparationTimeMinutes())
                .category(ProductResponse.CategorySummary.builder()
                        .id(product.getCategory().getId())
                        .categoryName(product.getCategory().getCategoryName())
                        .build())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private String slugify(String name) {
        if (name == null) return "";
        String slug = name.toLowerCase().trim();
        slug = slug.replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a");
        slug = slug.replaceAll("[éèẻẽẹêếềểễệ]", "e");
        slug = slug.replaceAll("[íìỉĩị]", "i");
        slug = slug.replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o");
        slug = slug.replaceAll("[úùủũụưứừửữự]", "u");
        slug = slug.replaceAll("[ýỳỷỹỵ]", "y");
        slug = slug.replaceAll("[đ]", "d");
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-+", "-");
        return slug;
    }
}
