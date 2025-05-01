package com.devstack.quickcart.user_service_api.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonFileSavedBinaryDataDTO {
    private Blob hash;
    private String directory;
    private Blob fileName;
    private Blob resourceUrl;
}
