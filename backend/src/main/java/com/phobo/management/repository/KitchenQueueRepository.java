package com.phobo.management.repository;

import com.phobo.management.entity.KitchenQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KitchenQueueRepository extends JpaRepository<KitchenQueue, String> {
    Optional<KitchenQueue> findByOrderItemId(String orderItemId);
    boolean existsByOrderItemId(String orderItemId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(k) FROM KitchenQueue k JOIN k.orderItem oi JOIN oi.order o WHERE o.id = :orderId AND k.itemStatus <> 'DA_XONG'")
    long countNonReadyItems(String orderId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(k) FROM KitchenQueue k JOIN k.orderItem oi JOIN oi.order o WHERE o.id = :orderId")
    long countItemsInQueue(String orderId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT k FROM KitchenQueue k WHERE k.id = :id")
    Optional<KitchenQueue> findByIdWithLock(String id);
}
