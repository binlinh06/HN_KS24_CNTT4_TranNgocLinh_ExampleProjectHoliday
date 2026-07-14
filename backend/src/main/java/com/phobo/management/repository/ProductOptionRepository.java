package com.phobo.management.repository;

import com.phobo.management.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, String> {
    List<ProductOption> findAllByGroupIdOrderByDisplayOrderAsc(String groupId);

    List<ProductOption> findAllByGroupIdAndIsAvailableTrueOrderByDisplayOrderAsc(String groupId);

    boolean existsByOptionNameIgnoreCaseAndGroupId(String optionName, String groupId);

    boolean existsByOptionNameIgnoreCaseAndGroupIdAndIdNot(String optionName, String groupId, String id);
}
