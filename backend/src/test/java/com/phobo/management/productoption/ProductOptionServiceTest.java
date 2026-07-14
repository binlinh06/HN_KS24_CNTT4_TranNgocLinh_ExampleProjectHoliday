package com.phobo.management.productoption;

import com.phobo.management.entity.OptionGroup;
import com.phobo.management.exception.BadRequestException;
import com.phobo.management.productoption.dto.OptionGroupRequest;
import com.phobo.management.productoption.dto.OptionGroupResponse;
import com.phobo.management.productoption.service.ProductOptionService;
import com.phobo.management.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductOptionServiceTest {

    @Mock
    private OptionGroupRepository optionGroupRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionGroupRepository productOptionGroupRepository;

    @Mock
    private OrderItemOptionRepository orderItemOptionRepository;

    @InjectMocks
    private ProductOptionService productOptionService;

    private OptionGroup group;
    private OptionGroupRequest request;

    @BeforeEach
    void setUp() {
        group = OptionGroup.builder()
                .id("group-123")
                .groupName("Kích thước tô")
                .isRequired(true)
                .minSelectable(1)
                .maxSelectable(1)
                .displayOrder(1)
                .isActive(true)
                .options(new ArrayList<>())
                .build();

        request = OptionGroupRequest.builder()
                .groupName("Kích thước tô")
                .isRequired(true)
                .minSelectable(1)
                .maxSelectable(1)
                .displayOrder(1)
                .isActive(true)
                .build();
    }

    @Test
    void createOptionGroup_success() {
        when(optionGroupRepository.existsByGroupNameIgnoreCase("Kích thước tô")).thenReturn(false);
        when(optionGroupRepository.save(any(OptionGroup.class))).thenReturn(group);

        OptionGroupResponse response = productOptionService.createOptionGroup(request);

        assertNotNull(response);
        assertEquals("Kích thước tô", response.getGroupName());
    }

    @Test
    void createOptionGroup_minGreaterThanMax_throwsBadRequest() {
        request.setMinSelectable(2);
        request.setMaxSelectable(1);

        assertThrows(BadRequestException.class, () -> productOptionService.createOptionGroup(request));
    }

    @Test
    void createOptionGroup_maxSelectableZero_throwsBadRequest() {
        request.setMinSelectable(0);
        request.setMaxSelectable(0);

        assertThrows(BadRequestException.class, () -> productOptionService.createOptionGroup(request));
    }
}
