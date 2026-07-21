package com.phobo.management.repository;

import com.phobo.management.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByOrderId(String orderId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
