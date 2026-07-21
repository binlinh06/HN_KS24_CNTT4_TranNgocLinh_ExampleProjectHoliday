package com.phobo.management.repository;

import com.phobo.management.entity.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {
    Optional<SystemConfiguration> findByConfigKey(String configKey);
    List<SystemConfiguration> findByIsPublicTrue();
    List<SystemConfiguration> findByConfigGroup(String configGroup);
}
