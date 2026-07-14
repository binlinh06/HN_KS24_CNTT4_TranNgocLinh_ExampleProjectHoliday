package com.phobo.management.repository;

import com.phobo.management.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findAllByDeletedAtIsNullOrderByDisplayOrderAsc();

    List<Category> findAllByDeletedAtIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    Optional<Category> findByIdAndDeletedAtIsNull(String id);

    boolean existsByCategoryNameIgnoreCaseAndDeletedAtIsNull(String categoryName);

    boolean existsByCategoryNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String categoryName, String id);
}
