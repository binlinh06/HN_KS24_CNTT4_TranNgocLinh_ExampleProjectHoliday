package com.phobo.management.repository;

import com.phobo.management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndDeletedAtIsNull(String id);

    Optional<Product> findBySlugAndDeletedAtIsNull(String slug);

    List<Product> findAllByDeletedAtIsNullAndIsAvailableTrueAndIsFeaturedTrue();

    boolean existsByProductNameIgnoreCaseAndDeletedAtIsNull(String productName);

    boolean existsByProductNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String productName, String id);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNullAndIdNot(String slug, String id);

    boolean existsByCategoryIdAndDeletedAtIsNull(String categoryId);
}
