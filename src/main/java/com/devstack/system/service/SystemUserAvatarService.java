package com.devstack.system.service;


import com.devstack.system.dto.request.RequestSystemUserAvatarDto;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;

public interface SystemUserAvatarService {
    void createSystemUserAvatar(RequestSystemUserAvatarDto dto,String email, MultipartFile file) throws SQLException;
}
