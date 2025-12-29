package com.stoliar.dto.user;

import lombok.Data;

@Data
public class UserApiResponse {
    private boolean success;
    private String message;
    private UserInfoDto data;
    private String timestamp;
}