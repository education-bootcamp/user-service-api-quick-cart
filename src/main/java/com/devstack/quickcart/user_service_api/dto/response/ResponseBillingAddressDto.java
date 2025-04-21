package com.devstack.quickcart.user_service_api.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class ResponseBillingAddressDto {
    private String id;
    private String country;
    private String city;
    private String street;
}
