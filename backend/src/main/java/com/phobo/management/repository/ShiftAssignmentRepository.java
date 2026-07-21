package com.phobo.management.repository;

import com.phobo.management.entity.ShiftAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, String> {

    List<ShiftAssignment> findByEmployeeIdAndWorkDate(String employeeId, LocalDate workDate);

    List<ShiftAssignment> findByEmployeeIdAndWorkDateBetween(String employeeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "(:employeeId IS NULL OR sa.employee.id = :employeeId) AND " +
           "(:startDate IS NULL OR sa.workDate >= :startDate) AND " +
           "(:endDate IS NULL OR sa.workDate <= :endDate) AND " +
           "(:status IS NULL OR sa.status = :status)")
    Page<ShiftAssignment> findAssignmentsWithFilters(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status,
            Pageable pageable);
}
