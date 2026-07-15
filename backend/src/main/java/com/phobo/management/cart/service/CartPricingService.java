package com.phobo.management.cart.service;

import com.phobo.management.entity.CartItem;
import com.phobo.management.entity.ProductOption;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@Service
public class CartPricingService {

    public BigDecimal calculateItemUnitPrice(CartItem item) {
        BigDecimal basePrice = item.getProduct().getBasePrice();
        BigDecimal optionsPrice = calculateOptionsPrice(item.getOptions());
        return basePrice.add(optionsPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateOptionsPrice(Collection<ProductOption> options) {
        if (options == null || options.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return options.stream()
                .map(ProductOption::getIncrementalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateLineTotal(CartItem item) {
        BigDecimal unitPrice = calculateItemUnitPrice(item);
        return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateSubtotal(Collection<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(this::calculateLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEstimatedTotal(BigDecimal subtotal, BigDecimal discountAmount) {
        BigDecimal total = subtotal.subtract(discountAmount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
