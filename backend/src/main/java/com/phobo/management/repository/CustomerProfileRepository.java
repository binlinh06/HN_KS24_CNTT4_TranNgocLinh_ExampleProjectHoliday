package com.phobo.management.repository;

import com.phobo.management.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, String> {
    
    Optional<CustomerProfile> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.id = :id")
    Optional<CustomerProfile> findByIdWithLock(String id);
}
