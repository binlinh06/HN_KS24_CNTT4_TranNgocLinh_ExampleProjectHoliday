package com.phobo.management.productoption.service;

import com.phobo.management.entity.OptionGroup;
import com.phobo.management.entity.Product;
import com.phobo.management.entity.ProductOption;
import com.phobo.management.entity.ProductOptionGroup;
import com.phobo.management.exception.BadRequestException;
import com.phobo.management.exception.ResourceNotFoundException;
import com.phobo.management.productoption.dto.OptionGroupRequest;
import com.phobo.management.productoption.dto.OptionGroupResponse;
import com.phobo.management.productoption.dto.OptionRequest;
import com.phobo.management.productoption.dto.OptionResponse;
import com.phobo.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductOptionService {

    private final OptionGroupRepository optionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;

    @Transactional(readOnly = true)
    public List<OptionGroupResponse> getProductOptions(String productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        return product.getOptionGroups().stream()
                .filter(OptionGroup::getIsActive)
                .sorted(Comparator.comparing(OptionGroup::getDisplayOrder))
                .map(group -> {
                    List<OptionResponse> options = group.getOptions().stream()
                            .filter(ProductOption::getIsAvailable)
                            .sorted(Comparator.comparing(ProductOption::getDisplayOrder))
                            .map(this::mapToOptionResponse)
                            .collect(Collectors.toList());

                    return mapToGroupResponse(group, options);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OptionGroupResponse> getAllOptionGroups() {
        return optionGroupRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(group -> {
                    List<OptionResponse> options = group.getOptions().stream()
                            .sorted(Comparator.comparing(ProductOption::getDisplayOrder))
                            .map(this::mapToOptionResponse)
                            .collect(Collectors.toList());
                    return mapToGroupResponse(group, options);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public OptionGroupResponse createOptionGroup(OptionGroupRequest request) {
        if (request.getMinSelectable() > request.getMaxSelectable()) {
            throw new BadRequestException("Số lượng chọn tối thiểu không được lớn hơn số lượng chọn tối đa");
        }
        if (request.getMaxSelectable() <= 0) {
            throw new BadRequestException("Số lượng chọn tối đa phải lớn hơn 0");
        }

        String normalizedName = request.getGroupName().trim();
        if (optionGroupRepository.existsByGroupNameIgnoreCase(normalizedName)) {
            throw new BadRequestException("Tên nhóm tùy chọn đã tồn tại");
        }

        OptionGroup group = OptionGroup.builder()
                .id(UUID.randomUUID().toString())
                .groupName(normalizedName)
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .minSelectable(request.getMinSelectable())
                .maxSelectable(request.getMaxSelectable())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        OptionGroup saved = optionGroupRepository.save(group);
        return mapToGroupResponse(saved, List.of());
    }

    @Transactional
    public OptionGroupResponse updateOptionGroup(String id, OptionGroupRequest request) {
        OptionGroup group = optionGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm tùy chọn"));

        if (request.getMinSelectable() > request.getMaxSelectable()) {
            throw new BadRequestException("Số lượng chọn tối thiểu không được lớn hơn số lượng chọn tối đa");
        }
        if (request.getMaxSelectable() <= 0) {
            throw new BadRequestException("Số lượng chọn tối đa phải lớn hơn 0");
        }

        String normalizedName = request.getGroupName().trim();
        if (optionGroupRepository.existsByGroupNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new BadRequestException("Tên nhóm tùy chọn đã tồn tại");
        }

        // Validate business rule: Nhóm bắt buộc phải có ít nhất một lựa chọn hoạt động
        if (request.getIsActive() != null && request.getIsActive()) {
            long activeOptionsCount = group.getOptions().stream().filter(ProductOption::getIsAvailable).count();
            if (activeOptionsCount == 0) {
                throw new BadRequestException("Nhóm hoạt động phải có ít nhất một lựa chọn hoạt động");
            }
        }

        group.setGroupName(normalizedName);
        if (request.getIsRequired() != null) {
            group.setIsRequired(request.getIsRequired());
        }
        group.setMinSelectable(request.getMinSelectable());
        group.setMaxSelectable(request.getMaxSelectable());
        if (request.getDisplayOrder() != null) {
            group.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            group.setIsActive(request.getIsActive());
        }

        OptionGroup updated = optionGroupRepository.save(group);
        List<OptionResponse> options = updated.getOptions().stream()
                .sorted(Comparator.comparing(ProductOption::getDisplayOrder))
                .map(this::mapToOptionResponse)
                .collect(Collectors.toList());

        return mapToGroupResponse(updated, options);
    }

    @Transactional
    public void deleteOptionGroup(String id) {
        OptionGroup group = optionGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm tùy chọn"));

        for (ProductOption option : group.getOptions()) {
            if (orderItemOptionRepository.existsByOptionId(option.getId())) {
                throw new BadRequestException("Không thể xóa dữ liệu vì đang được liên kết với dữ liệu nghiệp vụ khác");
            }
        }

        optionGroupRepository.delete(group);
    }

    @Transactional
    public OptionResponse addOption(String groupId, OptionRequest request) {
        OptionGroup group = optionGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm tùy chọn"));

        String normalizedName = request.getOptionName().trim();
        if (productOptionRepository.existsByOptionNameIgnoreCaseAndGroupId(normalizedName, groupId)) {
            throw new BadRequestException("Tên tùy chọn đã tồn tại trong nhóm này");
        }

        ProductOption option = ProductOption.builder()
                .id(UUID.randomUUID().toString())
                .group(group)
                .optionName(normalizedName)
                .incrementalPrice(request.getIncrementalPrice())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        ProductOption saved = productOptionRepository.save(option);
        return mapToOptionResponse(saved);
    }

    @Transactional
    public OptionResponse updateOption(String optionId, OptionRequest request) {
        ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tùy chọn"));

        String normalizedName = request.getOptionName().trim();
        if (productOptionRepository.existsByOptionNameIgnoreCaseAndGroupIdAndIdNot(normalizedName, option.getGroup().getId(), optionId)) {
            throw new BadRequestException("Tên tùy chọn đã tồn tại trong nhóm này");
        }

        // Validate business rule: Nhóm bắt buộc phải có ít nhất một lựa chọn hoạt động
        if (request.getIsAvailable() != null && !request.getIsAvailable()) {
            OptionGroup group = option.getGroup();
            if (group.getIsActive()) {
                long activeOptionsCount = group.getOptions().stream()
                        .filter(opt -> !opt.getId().equals(optionId) && opt.getIsAvailable())
                        .count();
                if (activeOptionsCount == 0) {
                    throw new BadRequestException("Nhóm hoạt động phải có ít nhất một lựa chọn hoạt động");
                }
            }
        }

        option.setOptionName(normalizedName);
        option.setIncrementalPrice(request.getIncrementalPrice());
        if (request.getIsAvailable() != null) {
            option.setIsAvailable(request.getIsAvailable());
        }
        if (request.getDisplayOrder() != null) {
            option.setDisplayOrder(request.getDisplayOrder());
        }

        ProductOption updated = productOptionRepository.save(option);
        return mapToOptionResponse(updated);
    }

    @Transactional
    public void deleteOption(String optionId) {
        ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tùy chọn"));

        if (orderItemOptionRepository.existsByOptionId(optionId)) {
            throw new BadRequestException("Không thể xóa dữ liệu vì đang được liên kết với dữ liệu nghiệp vụ khác");
        }

        // Validate business rule: Nhóm bắt buộc phải có ít nhất một lựa chọn hoạt động
        OptionGroup group = option.getGroup();
        if (group.getIsActive()) {
            long activeOptionsCount = group.getOptions().stream()
                    .filter(opt -> !opt.getId().equals(optionId) && opt.getIsAvailable())
                    .count();
            if (activeOptionsCount == 0) {
                throw new BadRequestException("Nhóm hoạt động phải có ít nhất một lựa chọn hoạt động");
            }
        }

        productOptionRepository.delete(option);
    }

    @Transactional
    public void assignGroupToProduct(String productId, String groupId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        OptionGroup group = optionGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm tùy chọn"));

        if (productOptionGroupRepository.existsByProductIdAndGroupId(productId, groupId)) {
            throw new BadRequestException("Nhóm tùy chọn đã được liên kết với sản phẩm này");
        }

        ProductOptionGroup link = new ProductOptionGroup(productId, groupId);
        productOptionGroupRepository.save(link);
    }

    @Transactional
    public void removeGroupFromProduct(String productId, String groupId) {
        if (!productOptionGroupRepository.existsByProductIdAndGroupId(productId, groupId)) {
            throw new ResourceNotFoundException("Không tìm thấy liên kết giữa sản phẩm và nhóm tùy chọn");
        }
        productOptionGroupRepository.deleteByProductIdAndGroupId(productId, groupId);
    }

    private OptionGroupResponse mapToGroupResponse(OptionGroup group, List<OptionResponse> options) {
        return OptionGroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .isRequired(group.getIsRequired())
                .minSelectable(group.getMinSelectable())
                .maxSelectable(group.getMaxSelectable())
                .displayOrder(group.getDisplayOrder())
                .isActive(group.getIsActive())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .options(options)
                .build();
    }

    private OptionResponse mapToOptionResponse(ProductOption option) {
        return OptionResponse.builder()
                .id(option.getId())
                .optionName(option.getOptionName())
                .incrementalPrice(option.getIncrementalPrice())
                .isAvailable(option.getIsAvailable())
                .displayOrder(option.getDisplayOrder())
                .createdAt(option.getCreatedAt())
                .updatedAt(option.getUpdatedAt())
                .build();
    }
}
