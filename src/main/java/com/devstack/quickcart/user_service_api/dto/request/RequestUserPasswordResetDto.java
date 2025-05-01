package com.devstack.quickcart.user_service_api.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RequestUserPasswordResetDto {
    private String email;
    private String code;
    private String password;
}
