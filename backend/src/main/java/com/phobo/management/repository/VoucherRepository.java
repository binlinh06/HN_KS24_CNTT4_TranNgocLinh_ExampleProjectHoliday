package com.phobo.management.repository;

import com.phobo.management.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT v FROM Voucher v WHERE LOWER(v.code) = LOWER(:code) AND v.deletedAt IS NULL")
    Optional<Voucher> findByCodeForUpdate(@org.springframework.data.repository.query.Param("code") String code);
}

