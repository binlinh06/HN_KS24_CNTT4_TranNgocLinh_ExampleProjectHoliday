package com.phobo.management.repository;

import com.phobo.management.entity.ProductOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductOptionGroupRepository extends JpaRepository<ProductOptionGroup, ProductOptionGroup.ProductOptionGroupId> {
    
    
    
    
    
    
}
