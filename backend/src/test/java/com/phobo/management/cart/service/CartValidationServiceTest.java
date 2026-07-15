package com.phobo.management.cart.service;

import com.phobo.management.entity.OptionGroup;
import com.phobo.management.entity.Product;
import com.phobo.management.entity.ProductOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CartValidationServiceTest {

    private CartValidationService cartValidationService;

    @BeforeEach
    public void setUp() {
        cartValidationService = new CartValidationService();
    }

    @Test
    public void testProductNotAvailable() {
        Product product = Product.builder().isAvailable(false).build();
        List<String> errors = cartValidationService.validateProductConfiguration(product, Collections.emptyList());
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("Món ăn này hiện không còn được bán."));
    }

    @Test
    public void testRequiredOptionGroupMissing() {
        OptionGroup requiredGroup = OptionGroup.builder()
                .id("group-1")
                .groupName("Kích thước")
                .isRequired(true)
                .minSelectable(1)
                .maxSelectable(1)
                .isActive(true)
                .build();

        Product product = Product.builder()
                .isAvailable(true)
                .optionGroups(new HashSet<>(Arrays.asList(requiredGroup)))
                .build();

        List<String> errors = cartValidationService.validateProductConfiguration(product, Collections.emptyList());
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("Vui lòng chọn đầy đủ các tùy chọn bắt buộc thuộc nhóm 'Kích thước'."));
    }
}
