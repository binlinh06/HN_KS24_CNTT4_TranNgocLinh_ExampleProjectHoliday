package com.phobo.management.repository;

import com.phobo.management.entity.Ingredient;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Ingredient i WHERE i.id = :id")
    Optional<Ingredient> findByIdForUpdate(@Param("id") String id);

    Optional<Ingredient> findByIngredientCode(String ingredientCode);

    boolean existsByIngredientCode(String ingredientCode);

    @Query("SELECT i FROM Ingredient i WHERE " +
           "(:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.ingredientCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:isActive IS NULL OR i.isActive = :isActive)")
    Page<Ingredient> findIngredientsWithFilters(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT i FROM Ingredient i WHERE i.isActive = true AND i.currentStock <= i.minThreshold")
    List<Ingredient> findLowStockIngredients();
}
