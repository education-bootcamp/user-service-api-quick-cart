package com.devstack.system.service;

import com.devstack.system.dto.request.RequestUserRoleDto;
import com.devstack.system.dto.response.ResponseUserRoleDTO;

import java.util.List;

public interface UserRoleService {
    void createUser(RequestUserRoleDto dto);

    List<ResponseUserRoleDTO> findAllUserRole();
}
