package com.phobo.management.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    @NotBlank(message = "Tên người nhận không được trống")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được trống")
    @Pattern(regexp = "^(0|\\+84)(\\s|\\.)?[35789]\\d{2}(\\s|\\.)?\\d{3}(\\s|\\.)?\\d{4}$", message = "Số điện thoại Việt Nam không hợp lệ")
    private String receiverPhone;

    @NotBlank(message = "Chi tiết địa chỉ không được trống")
    @Size(max = 500, message = "Chi tiết địa chỉ tối đa 500 ký tự")
    private String addressDetail;

    @Size(max = 100, message = "Nhãn địa chỉ tối đa 100 ký tự")
    private String addressLabel;

    private Boolean isDefault;
}
