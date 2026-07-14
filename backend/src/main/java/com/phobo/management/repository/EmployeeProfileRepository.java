package com.phobo.management.repository;

import com.phobo.management.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, String> {
    
    
    
    
    Optional<EmployeeProfile> findByUserId(String userId);
    
}
