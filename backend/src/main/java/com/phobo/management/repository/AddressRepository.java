package com.phobo.management.repository;

import com.phobo.management.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    java.util.List<Address> findByCustomerIdOrderByCreatedAtAsc(String customerId);
    java.util.Optional<Address> findByCustomerIdAndIsDefaultTrue(String customerId);
}
