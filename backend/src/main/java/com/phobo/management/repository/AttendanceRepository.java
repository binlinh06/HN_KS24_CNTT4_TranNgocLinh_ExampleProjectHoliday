package com.phobo.management.repository;

import com.phobo.management.entity.Attendance;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.checkOut IS NULL")
    Optional<Attendance> findActiveAttendanceForUpdate(@Param("employeeId") String employeeId);

    Optional<Attendance> findByEmployeeIdAndCheckOutIsNull(String employeeId);

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:employeeId IS NULL OR a.employee.id = :employeeId) AND " +
           "(:startDate IS NULL OR a.workDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.workDate <= :endDate) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Attendance> findAttendanceWithFilters(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status,
            Pageable pageable);
}
