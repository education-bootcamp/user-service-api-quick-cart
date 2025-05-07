package com.devstack.system.service.impl;

import com.devstack.system.config.KeycloakSecurityUtil;
import com.devstack.system.dto.request.RequestUserRoleDto;
import com.devstack.system.dto.response.ResponseUserRoleDTO;
import com.devstack.system.exception.DuplicateEntryException;
import com.devstack.system.service.UserRoleService;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {
    private final KeycloakSecurityUtil keycloakUtil;
    @Value("${keycloak.config.realm}")
    private String realm;

    @Value("${keycloak.config.client-id}")
    private String clientId;


    @Value("${keycloak.config.secret}")
    private String secret;

    @Value("${spring.security.oauth2.resourceserver.jwt.token-uri}")
    private String keyCloakApiUrl;

    @Override
    public void createUser(RequestUserRoleDto dto) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        RoleRepresentation roleRepresentation;
        try {
            roleRepresentation = keycloak.realm(realm).roles().get(dto.getRoleName()).toRepresentation();
            throw new DuplicateEntryException("Role already exists: " + dto.getRoleName());
        } catch (NotFoundException e) {
            RoleRepresentation newRole = new RoleRepresentation();
            newRole.setName(dto.getRoleName().toLowerCase());
            keycloak.realm(realm).roles().create(newRole);
        }



    }
    @Override
    public List<ResponseUserRoleDTO> findAllUserRole(){
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();

        List<ResponseUserRoleDTO> userRoles = new ArrayList<>();

        List<RoleRepresentation> roles = keycloak.realm(realm).roles().list();
        for (RoleRepresentation role : roles) {
            userRoles.add(ResponseUserRoleDTO.builder()
                    .roleName(role.getName())
                    .build());
        }

        return userRoles;
    }
}
