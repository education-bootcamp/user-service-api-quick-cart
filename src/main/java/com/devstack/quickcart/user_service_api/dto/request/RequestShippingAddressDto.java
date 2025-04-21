package com.devstack.quickcart.user_service_api.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class RequestShippingAddressDto {
    private String country;
    private String city;
    private String street;
}
