package com.devstack.quickcart.user_service_api.service;

import com.devstack.quickcart.user_service_api.util.CommonFileSavedBinaryDataDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    public CommonFileSavedBinaryDataDTO createResource(MultipartFile file, String directory, String bucket);
    public void deleteResource(String bucket,String directory, String fileName);
    public byte[] downloadFile(String bucket, String fileName);
}
