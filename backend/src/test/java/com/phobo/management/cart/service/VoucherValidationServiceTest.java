package com.phobo.management.cart.service;

import com.phobo.management.entity.Voucher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoucherValidationServiceTest {

    private VoucherValidationService voucherValidationService;

    @BeforeEach
    public void setUp() {
        voucherValidationService = new VoucherValidationService();
    }

    @Test
    public void testVoucherMinOrderValue() {
        Voucher voucher = Voucher.builder()
                .code("PHO10")
                .isActive(true)
                .minOrderValue(BigDecimal.valueOf(100000))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .usageLimit(100)
                .usedCount(0)
                .build();

        List<String> errors = voucherValidationService.validateVoucher(voucher, BigDecimal.valueOf(50000));
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("Giá trị đơn hàng chưa đạt tối thiểu 100000đ để áp dụng mã này."));
    }

    @Test
    public void testPercentageDiscount() {
        Voucher voucher = Voucher.builder()
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(10))
                .build();

        BigDecimal discount = voucherValidationService.calculateDiscount(voucher, BigDecimal.valueOf(150000));
        assertEquals(0, BigDecimal.valueOf(15000).compareTo(discount));
    }

    @Test
    public void testFixedDiscount() {
        Voucher voucher = Voucher.builder()
                .discountType("FIXED_AMOUNT")
                .discountValue(BigDecimal.valueOf(20000))
                .build();

        BigDecimal discount = voucherValidationService.calculateDiscount(voucher, BigDecimal.valueOf(150000));
        assertEquals(0, BigDecimal.valueOf(20000).compareTo(discount));
    }

    @Test
    public void testDiscountCannotExceedSubtotal() {
        Voucher voucher = Voucher.builder()
                .discountType("FIXED_AMOUNT")
                .discountValue(BigDecimal.valueOf(50000))
                .build();

        BigDecimal discount = voucherValidationService.calculateDiscount(voucher, BigDecimal.valueOf(30000));
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(discount));
    }
}
