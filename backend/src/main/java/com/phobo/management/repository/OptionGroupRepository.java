package com.phobo.management.repository;

import com.phobo.management.entity.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroup, String> {
    List<OptionGroup> findAllByOrderByDisplayOrderAsc();

    List<OptionGroup> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    boolean existsByGroupNameIgnoreCase(String groupName);

    boolean existsByGroupNameIgnoreCaseAndIdNot(String groupName, String id);
}
