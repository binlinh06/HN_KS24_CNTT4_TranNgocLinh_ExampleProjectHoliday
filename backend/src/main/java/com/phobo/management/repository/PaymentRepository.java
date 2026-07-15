package com.phobo.management.repository;

import com.phobo.management.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    java.util.Optional<Payment> findByOrderId(String orderId);
    java.util.Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Payment p WHERE p.id = :id")
    java.util.Optional<Payment> findByIdWithLock(String id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Payment p WHERE p.providerTransactionId = :providerTransactionId")
    java.util.Optional<Payment> findByProviderTransactionIdWithLock(String providerTransactionId);
}
