package com.phobo.management.repository;

import com.phobo.management.entity.CartItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartItemOptionRepository extends JpaRepository<CartItemOption, CartItemOption.CartItemOptionId> {
    
    
    
    
    
    
}
