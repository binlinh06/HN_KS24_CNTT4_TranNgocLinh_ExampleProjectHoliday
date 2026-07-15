package com.phobo.management.repository;

import com.phobo.management.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {

    Optional<IdempotencyRecord> findByCustomerIdAndOperationAndIdempotencyKey(
        String customerId, String operation, String idempotencyKey
    );

    @Modifying
    @Query(value = "INSERT IGNORE INTO idempotency_records (id, customer_id, idempotency_key, operation, request_hash, status, response_body, created_at, expires_at) " +
                   "VALUES (:id, :customerId, :idempotencyKey, :operation, :requestHash, 'PROCESSING', NULL, :createdAt, :expiresAt)", 
           nativeQuery = true)
    int claimIdempotencyKey(
        @Param("id") String id,
        @Param("customerId") String customerId,
        @Param("idempotencyKey") String idempotencyKey,
        @Param("operation") String operation,
        @Param("requestHash") String requestHash,
        @Param("createdAt") LocalDateTime createdAt,
        @Param("expiresAt") LocalDateTime expiresAt
    );

    @Modifying
    @Query("DELETE FROM IdempotencyRecord r WHERE r.expiresAt < :now")
    void deleteExpiredRecords(@Param("now") LocalDateTime now);
}
