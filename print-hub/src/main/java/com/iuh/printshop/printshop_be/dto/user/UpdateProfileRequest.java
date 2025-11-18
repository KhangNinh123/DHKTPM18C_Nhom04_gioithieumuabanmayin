package com.iuh.printshop.printshop_be.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 120, message = "Họ tên không được quá 120 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String defaultAddress;
}

