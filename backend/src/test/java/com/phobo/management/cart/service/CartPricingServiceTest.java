package com.phobo.management.cart.service;

import com.phobo.management.entity.CartItem;
import com.phobo.management.entity.Product;
import com.phobo.management.entity.ProductOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartPricingServiceTest {

    private CartPricingService cartPricingService;

    @BeforeEach
    public void setUp() {
        cartPricingService = new CartPricingService();
    }

    @Test
    public void testCalculateItemUnitPrice() {
        Product product = Product.builder().basePrice(BigDecimal.valueOf(50000)).build();
        ProductOption option1 = ProductOption.builder().incrementalPrice(BigDecimal.valueOf(10000)).build();
        ProductOption option2 = ProductOption.builder().incrementalPrice(BigDecimal.valueOf(5000)).build();
        
        CartItem item = CartItem.builder()
                .product(product)
                .options(new HashSet<>(Arrays.asList(option1, option2)))
                .quantity(2)
                .build();

        BigDecimal unitPrice = cartPricingService.calculateItemUnitPrice(item);
        assertEquals(0, BigDecimal.valueOf(65000).compareTo(unitPrice));
    }

    @Test
    public void testCalculateLineTotal() {
        Product product = Product.builder().basePrice(BigDecimal.valueOf(50000)).build();
        ProductOption option = ProductOption.builder().incrementalPrice(BigDecimal.valueOf(10000)).build();
        
        CartItem item = CartItem.builder()
                .product(product)
                .options(new HashSet<>(Arrays.asList(option)))
                .quantity(3)
                .build();

        BigDecimal lineTotal = cartPricingService.calculateLineTotal(item);
        assertEquals(0, BigDecimal.valueOf(180000).compareTo(lineTotal));
    }

    @Test
    public void testCalculateSubtotal() {
        Product product = Product.builder().basePrice(BigDecimal.valueOf(50000)).build();
        
        CartItem item1 = CartItem.builder().product(product).quantity(2).options(new HashSet<>()).build();
        CartItem item2 = CartItem.builder().product(product).quantity(1).options(new HashSet<>()).build();

        BigDecimal subtotal = cartPricingService.calculateSubtotal(Arrays.asList(item1, item2));
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(subtotal));
    }
}
