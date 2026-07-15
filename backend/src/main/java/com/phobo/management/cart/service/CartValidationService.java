package com.phobo.management.cart.service;

import com.phobo.management.entity.OptionGroup;
import com.phobo.management.entity.Product;
import com.phobo.management.entity.ProductOption;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartValidationService {

    public List<String> validateProductConfiguration(Product product, Collection<ProductOption> selectedOptions) {
        List<String> messages = new ArrayList<>();

        if (product == null) {
            messages.add("Món ăn không tồn tại.");
            return messages;
        }

        if (product.getDeletedAt() != null) {
            messages.add("Món ăn này đã bị xóa.");
            return messages;
        }

        if (Boolean.FALSE.equals(product.getIsAvailable())) {
            messages.add("Món ăn này hiện không còn được bán.");
        }

        // Build a map of option group id -> OptionGroup assigned to this product
        Set<OptionGroup> assignedGroups = product.getOptionGroups();
        if (assignedGroups == null) {
            assignedGroups = Collections.emptySet();
        }
        Map<String, OptionGroup> groupMap = assignedGroups.stream()
                .collect(Collectors.toMap(OptionGroup::getId, g -> g));

        // Group selected options by their option group id
        Map<String, List<ProductOption>> selectedByGroup = new HashMap<>();
        for (ProductOption option : selectedOptions) {
            if (option.getGroup() == null) {
                messages.add("Tùy chọn '" + option.getOptionName() + "' không thuộc nhóm hợp lệ.");
                continue;
            }
            String groupId = option.getGroup().getId();
            
            // Check if option's group is assigned to product
            if (!groupMap.containsKey(groupId)) {
                messages.add("Tùy chọn '" + option.getOptionName() + "' không thuộc về món ăn này.");
            }

            // Check if option is available
            if (Boolean.FALSE.equals(option.getIsAvailable())) {
                messages.add("Tùy chọn '" + option.getOptionName() + "' hiện đã hết hàng.");
            }

            selectedByGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(option);
        }

        // Verify min/max selections for each assigned option group
        for (OptionGroup group : assignedGroups) {
            if (Boolean.FALSE.equals(group.getIsActive())) {
                continue;
            }
            List<ProductOption> selectedInGroup = selectedByGroup.getOrDefault(group.getId(), Collections.emptyList());
            int count = selectedInGroup.size();

            int min = (group.getMinSelectable() != null) ? group.getMinSelectable() : (group.getIsRequired() ? 1 : 0);
            if (min > 0 && count < min) {
                messages.add("Vui lòng chọn đầy đủ các tùy chọn bắt buộc thuộc nhóm '" + group.getGroupName() + "'.");
            }

            if (group.getMaxSelectable() != null && count > group.getMaxSelectable()) {
                messages.add("Nhóm '" + group.getGroupName() + "' chỉ cho phép chọn tối đa " + group.getMaxSelectable() + " tùy chọn.");
            }
        }

        return messages;
    }
}
