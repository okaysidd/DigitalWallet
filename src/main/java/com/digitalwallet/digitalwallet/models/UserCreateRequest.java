package com.digitalwallet.digitalwallet.models;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String email;

    private String phone;
}
