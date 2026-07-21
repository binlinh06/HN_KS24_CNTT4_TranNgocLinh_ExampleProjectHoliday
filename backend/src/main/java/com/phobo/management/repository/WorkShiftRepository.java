package com.phobo.management.repository;

import com.phobo.management.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, String> {
    Optional<WorkShift> findByShiftCode(String shiftCode);
    boolean existsByShiftCode(String shiftCode);
    List<WorkShift> findByIsActiveTrue();
}
