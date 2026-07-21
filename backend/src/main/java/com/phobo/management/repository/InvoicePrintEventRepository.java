package com.phobo.management.repository;

import com.phobo.management.entity.InvoicePrintEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoicePrintEventRepository extends JpaRepository<InvoicePrintEvent, String> {
    List<InvoicePrintEvent> findByInvoiceId(String invoiceId);
}
