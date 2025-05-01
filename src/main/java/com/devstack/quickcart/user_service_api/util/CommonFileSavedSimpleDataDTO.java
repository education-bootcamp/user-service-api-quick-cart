package com.devstack.quickcart.user_service_api.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonFileSavedSimpleDataDTO {
    private String hash;
    private String directory;
    private String fileName;
    private String resourceUrl;
}
