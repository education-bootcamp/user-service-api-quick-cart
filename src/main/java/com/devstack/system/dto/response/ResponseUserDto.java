package com.devstack.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseUserDto {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
}
