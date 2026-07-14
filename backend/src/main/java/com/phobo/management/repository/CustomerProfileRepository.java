package com.phobo.management.repository;

import com.phobo.management.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, String> {
    
    
    
    Optional<CustomerProfile> findByUserId(String userId);
    
    
}
