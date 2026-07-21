package com.phobo.management.repository;

import com.phobo.management.entity.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE LOWER(v.code) = LOWER(:code) AND v.deletedAt IS NULL")
    Optional<Voucher> findByCodeForUpdate(@Param("code") String code);
}
