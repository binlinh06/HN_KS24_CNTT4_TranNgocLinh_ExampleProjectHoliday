package com.phobo.management.cart.service;

import com.phobo.management.entity.Voucher;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoucherValidationService {

    public List<String> validateVoucher(Voucher voucher, BigDecimal subtotal) {
        List<String> messages = new ArrayList<>();

        if (voucher == null) {
            messages.add("Mã giảm giá không tồn tại.");
            return messages;
        }

        if (Boolean.FALSE.equals(voucher.getIsActive())) {
            messages.add("Mã giảm giá hiện không hoạt động.");
        }

        if (voucher.getDeletedAt() != null) {
            messages.add("Mã giảm giá đã bị xóa.");
            return messages;
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            messages.add("Mã giảm giá chưa đến thời gian sử dụng.");
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            messages.add("Mã giảm giá đã hết hạn.");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            messages.add("Mã giảm giá đã hết lượt sử dụng.");
        }

        if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            messages.add("Giá trị đơn hàng chưa đạt tối thiểu " + voucher.getMinOrderValue().longValue() + "đ để áp dụng mã này.");
        }

        return messages;
    }

    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal subtotal) {
        if (voucher == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        String type = voucher.getDiscountType();
        BigDecimal value = voucher.getDiscountValue();

        if ("PERCENTAGE".equalsIgnoreCase(type)) {
            discount = subtotal.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("FIXED_AMOUNT".equalsIgnoreCase(type)) {
            discount = value;
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}
