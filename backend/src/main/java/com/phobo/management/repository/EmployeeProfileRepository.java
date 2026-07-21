package com.phobo.management.repository;

import com.phobo.management.entity.EmployeeProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, String> {

    Optional<EmployeeProfile> findByUserId(String userId);

    Optional<EmployeeProfile> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ep FROM EmployeeProfile ep WHERE ep.id = :id")
    Optional<EmployeeProfile> findByIdForUpdate(@Param("id") String id);

    @Query("SELECT ep FROM EmployeeProfile ep WHERE " +
           "(:keyword IS NULL OR LOWER(ep.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(ep.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(ep.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:position IS NULL OR ep.position = :position) AND " +
           "(:isActive IS NULL OR ep.isActive = :isActive)")
    Page<EmployeeProfile> findEmployeesWithFilters(
            @Param("keyword") String keyword,
            @Param("position") String position,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
