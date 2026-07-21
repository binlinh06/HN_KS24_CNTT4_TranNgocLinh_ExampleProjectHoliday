package com.phobo.management.invoice.service;

import com.phobo.management.entity.EmployeeProfile;
import com.phobo.management.entity.Invoice;
import com.phobo.management.entity.InvoicePrintEvent;
import com.phobo.management.exception.OrderException;
import com.phobo.management.repository.EmployeeProfileRepository;
import com.phobo.management.repository.InvoiceRepository;
import com.phobo.management.repository.InvoicePrintEventRepository;
import com.phobo.management.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoicePrintEventRepository invoicePrintEventRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    private EmployeeProfile getCurrentEmployeeProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new OrderException("Bạn phải đăng nhập để thực hiện thao tác này", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal)) {
            throw new OrderException("Thông tin xác thực không hợp lệ", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String userId = ((CustomUserPrincipal) principal).getId();
        return employeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OrderException("Không tìm thấy hồ sơ nhân viên", "EMPLOYEE_PROFILE_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceById(String invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new OrderException("Không tìm thấy hóa đơn", "INVOICE_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceByOrderId(String orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderException("Không tìm thấy hóa đơn cho đơn hàng này", "INVOICE_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Invoice markInvoicePrinted(String invoiceId, String reason) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new OrderException("Không tìm thấy hóa đơn", "INVOICE_NOT_FOUND", HttpStatus.NOT_FOUND));

        EmployeeProfile employee = getCurrentEmployeeProfile();

        // Increment print count
        invoice.setPrintCount(invoice.getPrintCount() + 1);
        invoice.setLastPrintedAt(LocalDateTime.now());
        Invoice saved = invoiceRepository.save(invoice);

        // Record print event
        InvoicePrintEvent printEvent = InvoicePrintEvent.builder()
                .id(UUID.randomUUID().toString())
                .invoice(invoice)
                .printedBy(employee)
                .printedAt(LocalDateTime.now())
                .printReason(reason)
                .build();
        invoicePrintEventRepository.save(printEvent);

        log.info("Recorded print event for invoice {}. Print count: {}", invoiceId, saved.getPrintCount());
        return saved;
    }
}
