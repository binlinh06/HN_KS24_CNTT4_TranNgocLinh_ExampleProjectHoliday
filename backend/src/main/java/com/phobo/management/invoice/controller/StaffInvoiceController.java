package com.phobo.management.invoice.controller;

import com.phobo.management.common.dto.ApiResponse;
import com.phobo.management.entity.Invoice;
import com.phobo.management.entity.OrderEntity;
import com.phobo.management.invoice.dto.InvoiceItemResponse;
import com.phobo.management.invoice.dto.InvoiceResponse;
import com.phobo.management.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/staff/invoices")
@RequiredArgsConstructor
@Slf4j
public class StaffInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{invoiceId}")
    @PreAuthorize("@employeeAuthService.isCashier()")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable String invoiceId) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(invoice), "Lấy thông tin hóa đơn thành công"));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("@employeeAuthService.isCashier()")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByOrderId(@PathVariable String orderId) {
        Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(invoice), "Lấy thông tin hóa đơn thành công"));
    }

    @PostMapping("/{invoiceId}/mark-printed")
    @PreAuthorize("@employeeAuthService.isCashier()")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markPrinted(
            @PathVariable String invoiceId,
            @RequestBody Map<String, String> payload) {
        String reason = payload != null ? payload.get("reason") : "";
        log.info("Marking invoice {} as printed. Reason: {}", invoiceId, reason);
        Invoice invoice = invoiceService.markInvoicePrinted(invoiceId, reason);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(invoice), "Cập nhật in hóa đơn thành công"));
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        OrderEntity order = invoice.getOrder();
        List<InvoiceItemResponse> items = order.getItems().stream()
                .map(item -> {
                    List<String> opts = item.getOptions().stream()
                            .map(o -> o.getOptionGroupNameSnapshot() + ": " + o.getOptionNameSnapshot())
                            .collect(Collectors.toList());
                    BigDecimal itemTotal = item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return InvoiceItemResponse.builder()
                            .productName(item.getProductNameSnapshot())
                            .quantity(item.getQuantity())
                            .price(item.getPriceAtOrder())
                            .total(itemTotal)
                            .options(opts)
                            .build();
                })
                .collect(Collectors.<InvoiceItemResponse>toList());

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .orderType(order.getOrderType().name())
                .tableNumber(order.getTable() != null ? order.getTable().getTableNumber() : null)
                .issuedByName(invoice.getIssuedBy().getFullName())
                .subtotal(invoice.getSubtotal())
                .discountAmount(invoice.getDiscountAmount())
                .finalAmount(invoice.getFinalAmount())
                .paymentMethod(invoice.getPaymentMethod())
                .issuedAt(invoice.getIssuedAt())
                .printCount(invoice.getPrintCount())
                .lastPrintedAt(invoice.getLastPrintedAt())
                .items(items)
                .build();
    }
}
