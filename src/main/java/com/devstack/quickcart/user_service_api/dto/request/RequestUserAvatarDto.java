package com.devstack.quickcart.user_service_api.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class RequestUserAvatarDto {
    private MultipartFile file;
}
