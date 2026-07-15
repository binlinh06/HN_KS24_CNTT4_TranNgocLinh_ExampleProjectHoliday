package com.phobo.management.repository;

import com.phobo.management.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCodeIgnoreCaseAndDeletedAtIsNull(String code);
}

