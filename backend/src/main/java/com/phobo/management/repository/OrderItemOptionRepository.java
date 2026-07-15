package com.phobo.management.repository;

import com.phobo.management.entity.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, String> {
    boolean existsByOptionId(String optionId);
}
