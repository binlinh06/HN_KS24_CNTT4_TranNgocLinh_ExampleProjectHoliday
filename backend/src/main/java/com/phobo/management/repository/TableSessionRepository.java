package com.phobo.management.repository;

import com.phobo.management.entity.TableSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface TableSessionRepository extends JpaRepository<TableSession, String> {

    @Query("SELECT s FROM TableSession s WHERE s.table.id = :tableId AND s.status IN ('OPEN', 'PAYMENT_PENDING')")
    Optional<TableSession> findActiveSessionByTableId(String tableId);

    Optional<TableSession> findByOrderId(String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TableSession s WHERE s.id = :id")
    Optional<TableSession> findByIdWithLock(String id);
}
