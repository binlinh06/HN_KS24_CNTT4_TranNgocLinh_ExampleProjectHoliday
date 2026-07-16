package com.phobo.management.repository;

import com.phobo.management.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    Optional<Review> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
    List<Review> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
