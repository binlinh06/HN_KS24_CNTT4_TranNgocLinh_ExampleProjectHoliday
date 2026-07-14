package com.phobo.management.repository;

import com.phobo.management.entity.ProductOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductOptionGroupRepository extends JpaRepository<ProductOptionGroup, ProductOptionGroup.ProductOptionGroupId> {
    @Transactional
    void deleteByProductIdAndGroupId(String productId, String groupId);

    boolean existsByProductIdAndGroupId(String productId, String groupId);
}
