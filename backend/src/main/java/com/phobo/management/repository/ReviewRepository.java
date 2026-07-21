package com.phobo.management.repository;

import com.phobo.management.common.enums.ReviewModerationStatus;
import com.phobo.management.entity.Review;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    Optional<Review> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    List<Review> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findByIdForUpdate(@Param("id") String id);

    @Query("SELECT r FROM Review r WHERE " +
           "(:status IS NULL OR r.moderationStatus = :status) AND " +
           "(:rating IS NULL OR r.rating = :rating) AND " +
           "(:from IS NULL OR r.createdAt >= :from) AND " +
           "(:to IS NULL OR r.createdAt <= :to)")
    Page<Review> findReviewsWithFilters(
            @Param("status") ReviewModerationStatus status,
            @Param("rating") Integer rating,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.moderationStatus = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<Review> findApprovedPublicReviews(Pageable pageable);
}
