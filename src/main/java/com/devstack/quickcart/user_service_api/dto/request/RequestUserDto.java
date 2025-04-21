package com.devstack.quickcart.user_service_api.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class RequestUserDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}
