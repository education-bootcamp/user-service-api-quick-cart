package com.devstack.quickcart.user_service_api.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class EmailTemplateHelper {
    public String loadHtmlTemplate(String templateFileName) {
        try {
            ClassPathResource resource = new ClassPathResource(templateFileName);
            byte[] fileData = resource.getInputStream().readAllBytes();
            return new String(fileData, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return ""; // Handle error appropriately
        }
    }
}
