package com.devstack.quickcart.user_service_api.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Builder
public class ResponseUserDto {
    private String username;
    private String firstName;
    private String lastName;
    private boolean activeStatus;
    private ResponseUserAvatarDto avatar; // if an avatar exists, return the actual value if not the system must send a null value
    private ResponseBillingAddressDto billingAddress; // if value exists ? value : null
    private ResponseShippingAddressDto shippingAddress; // if value exists ? value : null
}
