package com.devstack.system.dto.request;

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
