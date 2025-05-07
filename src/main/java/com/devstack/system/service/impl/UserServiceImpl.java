package com.devstack.system.service.impl;

import com.devstack.system.config.KeycloakSecurityUtil;
import com.devstack.system.dto.request.RequestRoleDto;
import com.devstack.system.dto.request.RequestUserDto;
import com.devstack.system.dto.request.RequestUserLoginRequest;
import com.devstack.system.dto.request.RequestUserPasswordResetDto;
import com.devstack.system.dto.response.ResponseUserDetailsDto;
import com.devstack.system.dto.response.ResponseUserDto;
import com.devstack.system.entity.Otp;
import com.devstack.system.entity.SystemUser;
import com.devstack.system.entity.SystemUserAvatar;
import com.devstack.system.exception.*;
import com.devstack.system.repository.OtpRepo;
import com.devstack.system.repository.SystemUserRepo;
import com.devstack.system.service.EmailService;
import com.devstack.system.service.UserService;
import com.devstack.system.util.FileDataExtractor;
import com.devstack.system.util.OtpGenerator;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakSecurityUtil keycloakUtil;

    private final SystemUserRepo systemUserRepo;
    private final EmailService emailService;
    private final OtpRepo otpRepo;
    private final JwtService jwtService;
    private final OtpGenerator otpGenerator;
    private final FileDataExtractor fileDataExtractor;

    @Value("${keycloak.config.realm}")
    private String realm;

    @Value("${keycloak.config.client-id}")
    private String clientId;


    @Value("${keycloak.config.secret}")
    private String secret;

    @Value("${spring.security.oauth2.resourceserver.jwt.token-uri}")
    private String keyCloakApiUrl;


    @Override
    public void createUser(RequestUserDto dto) throws IOException {

        String userId = "";
        String otpId = "";
        Keycloak keycloak = null;

        UserRepresentation existingUser = null;
        keycloak = keycloakUtil.getKeycloakInstance();
        System.out.println(keycloak);
        // Check if user already exists
        try {
            existingUser = keycloak.realm(realm).users().search(dto.getEmail()).stream()
                    .findFirst().orElse(null);
        } catch (WebApplicationException e) {
            throw new InternalServerException("Failed to connect to Keycloak: " + e.getMessage());
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error during user lookup: " + e.getMessage());
        }

        if (existingUser != null) {
            Optional<SystemUser> byEmail = systemUserRepo.findByEmail(existingUser.getEmail());
            if (byEmail.isEmpty()) {
                keycloak.realm(realm).users().delete(existingUser.getId());
            } else {
                throw new DuplicateEntryException("User with email " + dto.getEmail() + " already exists.");
            }

        } else {
            Optional<SystemUser> byEmail = systemUserRepo.findByEmail(dto.getEmail());
            if (byEmail.isPresent()) {
                Optional<Otp> bySystemUserId = otpRepo.findBySystemUserId(byEmail.get().getPropertyId());
                if (bySystemUserId.isPresent()) {
                    otpRepo.deleteById(bySystemUserId.get().getPropertyId());
                }
                systemUserRepo.deleteById(byEmail.get().getPropertyId());
            }
        }

        UserRepresentation userRep = mapUserRep(dto);
        Response res = keycloak.realm(realm).users().create(userRep);
        // Add the admin role to the newly created user
        if (res.getStatus() == Response.Status.CREATED.getStatusCode()) {
            RoleRepresentation userRole = keycloak.realm(realm).roles().get("user").toRepresentation();
            userId = res.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Arrays.asList(userRole));
            SystemUser createdSystemUser = SystemUser.builder()
                    .propertyId(userId)
                    .activeState(false)
                    .email(dto.getEmail())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .isAccountNonExpired(true)
                    .isEmailVerified(false)
                    .isAccountNonLocked(true)
                    .isEnabled(false)
                    .createdDate(new Date())
                    .build();
            SystemUser savedUser = systemUserRepo.save(createdSystemUser);
            Otp otp = Otp.builder()
                    .propertyId(UUID.randomUUID().toString())
                    .code(otpGenerator.generateOtp(4))
                    .createdDate(new Date())
                    .isVerified(false)
                    .attempts(0)
                    .systemUser(savedUser)
                    .build();
            otpRepo.save(otp);
            emailService.sendUserSignupVerificationCode(dto.getEmail(),
                    "Verify Your Email Address for Developers Stack Access", otp.getCode());
        }
    }

    @Override
    public void resend(String email) {
        try {
            Optional<SystemUser> selectedUserObj = systemUserRepo.findByEmail(email);
            if (selectedUserObj.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
            }
            SystemUser systemUser = selectedUserObj.get();
            if (systemUser.getIsEmailVerified()) {
                throw new DuplicateEntryException("The email is already activated");
            }
            Otp selectedOtpObj = systemUser.getOtp();
            if (selectedOtpObj.getAttempts() >= 5) {
                String code = otpGenerator.generateOtp(4);


                emailService.sendUserSignupVerificationCode(email,
                        "Verify Your Email Address for Developers Stack Access", code);


                selectedOtpObj.setAttempts(0);
                selectedOtpObj.setCode(code);
                selectedOtpObj.setCreatedDate(new Date());
                otpRepo.save(selectedOtpObj);

                throw new TooManyRequestException("Too many unsuccessful attempts. New OTP sent and please verify.");
            }
            emailService.sendUserSignupVerificationCode(systemUser.getEmail(),
                    "Verify Your Email Address for Developers Stack Access", selectedOtpObj.getCode());

        } catch (Exception e) {

            if (e instanceof DuplicateEntryException) {
                throw new DuplicateEntryException("The email is already activated");
            } else if (e instanceof TooManyRequestException) {
                throw new TooManyRequestException("Too many unsuccessful attempts. New OTP sent and please verify.");
            } else if (e instanceof EntryNotFoundException) {
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
            } else {
                throw new UnauthorizedException("Invalid username or password. Please double-check your credentials and try again.");
            }

        }
    }

    @Override
    public void forgotPasswordSendVerificationCode(String email) {

        Optional<SystemUser> selectedUserObj = systemUserRepo.findByEmail(email);
        if (selectedUserObj.isEmpty()) {
            throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
        }
        SystemUser systemUser = selectedUserObj.get();
        Keycloak keycloak = null;
        keycloak = keycloakUtil.getKeycloakInstance();
        UserRepresentation existingUser = keycloak.realm(realm).users().search(email).stream()
                .findFirst().orElse(null);
        if (existingUser == null) {
            throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
        }

        Otp selectedOtpObj = systemUser.getOtp();
        String code = otpGenerator.generateOtp(4);
        selectedOtpObj.setCode(code);
        selectedOtpObj.setCreatedDate(new Date());
        otpRepo.save(selectedOtpObj);
        try {
            emailService.sendPasswordResetVerificationCode(systemUser.getEmail(),
                    "Verify Your Email Address for Developers Stack Access", selectedOtpObj.getCode());
        } catch (IOException e) {
            throw new UnauthorizedException("Invalid username or password. Please double-check your credentials and try again.");

        }


    }

    @Override
    public boolean verifyReset(String otp, String email) {

        try {
            Optional<SystemUser> selectedUserObj = systemUserRepo.findByEmail(email);
            if (selectedUserObj.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
            }
            SystemUser systemUser = selectedUserObj.get();
            Otp selectedOtpObj = systemUser.getOtp();

            if (selectedOtpObj.getCode().equals(otp)) {

                return true;
            }
        } catch (Exception e) {
            if (e instanceof EntryNotFoundException) {
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
            } else {
                throw new InternalServerException("Something went wrong please try again later..");
            }

        }
        return false;
    }

    @Override
    public boolean passwordReset(RequestUserPasswordResetDto dto) {
        Optional<SystemUser> selectedUserObj = systemUserRepo.findByEmail(dto.getEmail());
        if (selectedUserObj.isPresent()) {
            SystemUser systemUser = selectedUserObj.get();
            Otp selectedOtpObj = systemUser.getOtp();
            Keycloak keycloak = keycloakUtil.getKeycloakInstance();
            List<UserRepresentation> keycloakUsers = keycloak.realm(realm).users().search(systemUser.getEmail());
            if (!keycloakUsers.isEmpty() && selectedOtpObj.getCode().equals(dto.getCode())) {
                UserRepresentation keycloakUser = keycloakUsers.get(0);
                UserResource userResource = keycloak.realm(realm).users().get(keycloakUser.getId());
                CredentialRepresentation newPassword = new CredentialRepresentation();
                newPassword.setType(CredentialRepresentation.PASSWORD);
                newPassword.setValue(dto.getPassword());
                newPassword.setTemporary(false);
                userResource.resetPassword(newPassword);

                return true;
            }

            throw new BadRequestException("Something went wrong with the OTP, Please try again");

        }
        throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");


    }

    @Override
    public Long trustedUserCount() {
        return systemUserRepo.count();
    }

    @Override
    public String getUserId(String email) {
        Optional<SystemUser> byEmail = systemUserRepo.findByEmail(email);
        if (byEmail.isEmpty()) {
            throw new EntryNotFoundException("User was not found");
        }
        return byEmail.get().getPropertyId();
    }

    @Override
    public boolean verifyEmail(String otp, String email) {
        try {
            Optional<SystemUser> selectedUserObj = systemUserRepo.findByEmail(email);
            if (selectedUserObj.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
            }
            SystemUser systemUser = selectedUserObj.get();

            Otp selectedOtpObj = systemUser.getOtp();

            if (selectedOtpObj.getIsVerified()) {
                throw new BadRequestException("This OTP has already been used. Please request another one for verification.");
            }

            if (selectedOtpObj.getAttempts() >= 5) {
                String code = otpGenerator.generateOtp(4);

                emailService.sendUserSignupVerificationCode(email,
                        "Verify Your Email Address for Developers Stack Access", code);

                selectedOtpObj.setAttempts(0);
                selectedOtpObj.setCode(code);
                selectedOtpObj.setCreatedDate(new Date());
                otpRepo.save(selectedOtpObj);

                throw new TooManyRequestException("Too many unsuccessful attempts. New OTP sent and please verify.");
            }

            if (selectedOtpObj.getCode().equals(otp)) {

                UserRepresentation keycloakUser = keycloakUtil.getKeycloakInstance().realm(realm)
                        .users()
                        .search(email)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new EntryNotFoundException("User not found! Contact support for assistance"));

                keycloakUser.setEmailVerified(true);
                keycloakUser.setEnabled(true);

                keycloakUtil.getKeycloakInstance().realm(realm)
                        .users()
                        .get(keycloakUser.getId())
                        .update(keycloakUser);

                systemUser.setActiveState(true);
                systemUser.setIsEnabled(true);
                systemUser.setIsEmailVerified(true);

                systemUserRepo.save(systemUser);

                selectedOtpObj.setIsVerified(true);
                selectedOtpObj.setAttempts(selectedOtpObj.getAttempts() + 1);

                otpRepo.save(selectedOtpObj);

                return true;

            } else {
                selectedOtpObj.setAttempts(selectedOtpObj.getAttempts() + 1);
                otpRepo.save(selectedOtpObj);
            }


        } catch (IOException exception) {
            throw new InternalServerException("Something went wrong please try again later..");
        }
        return false;
    }

    @Override
    public Object userLogin(RequestUserLoginRequest request) {
        try {
            Optional<SystemUser> selectedUserObj = systemUserRepo.findByEmail(request.getUsername());
            SystemUser systemUser = selectedUserObj.get();
            if (!systemUser.getIsEmailVerified()) {

                Otp selectedOtpObj = systemUser.getOtp();
                if (selectedOtpObj.getAttempts() >= 5) {

                    String code = otpGenerator.generateOtp(4);

                    emailService.sendUserSignupVerificationCode(systemUser.getEmail(),
                            "Verify Your Email Address for Developers Stack Access", code);

                    selectedOtpObj.setAttempts(0);
                    selectedOtpObj.setCode(code);
                    selectedOtpObj.setCreatedDate(new Date());
                    otpRepo.save(selectedOtpObj);

                    throw new TooManyRequestException("Too many unsuccessful attempts. New OTP sent and please verify.");
                }
                emailService.sendUserSignupVerificationCode(systemUser.getEmail(),
                        "Verify Your Email Address for Developers Stack Access", selectedOtpObj.getCode());
                throw new RedirectionException("Your email has not been verified. Please verify your email");

            } else {
                MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
                requestBody.add("client_id", clientId);
                requestBody.add("grant_type", OAuth2Constants.PASSWORD);
                requestBody.add("username", request.getUsername());
                requestBody.add("client_secret", secret);
                requestBody.add("password", request.getPassword());
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Object> response = restTemplate.postForEntity(keyCloakApiUrl, requestBody, Object.class);
                return response.getBody();
            }

        } catch (Exception e) {
            System.out.println(e);
            if (e instanceof RedirectionException) {
                throw new RedirectionException("Your email has not been verified. Please verify your email"+e.toString());
            } else if (e instanceof TooManyRequestException) {
                throw new TooManyRequestException("Too many unsuccessful attempts. New OTP sent and please verify."+e.toString());
            } else {
                throw new UnauthorizedException("Invalid username or password. Please double-check your credentials and try again."+e.toString());
            }

        }

    }

    @Override
    public boolean verifyAdmin(String email) {
        UserRepresentation keycloakUser = keycloakUtil.getKeycloakInstance().realm(realm)
                .users()
                .search(email)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntryNotFoundException("User not found! Contact support for assistance"));

        List<RoleRepresentation> roleRepresentations = keycloakUtil.getKeycloakInstance().realm(realm).users()
                .get(keycloakUser.getId()).roles().realmLevel().listAll();

        for (RequestRoleDto requestRole : mapRoles(roleRepresentations)
        ) {
            if (requestRole.getName().equals("admin")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean verifyStudent(String email) {
        UserRepresentation keycloakUser = keycloakUtil.getKeycloakInstance().realm(realm)
                .users()
                .search(email)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntryNotFoundException("User not found! Contact support for assistance"));

        List<RoleRepresentation> roleRepresentations = keycloakUtil.getKeycloakInstance().realm(realm).users()
                .get(keycloakUser.getId()).roles().realmLevel().listAll();

        for (RequestRoleDto requestRole : mapRoles(roleRepresentations)
        ) {
            if (requestRole.getName().equals("student")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean verifyRole(String email, String role) {
        System.out.println(email);
        UserRepresentation keycloakUser = keycloakUtil.getKeycloakInstance().realm(realm)
                .users()
                .search(email)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntryNotFoundException("User not found! Contact support for assistance"));

        List<RoleRepresentation> roleRepresentations = keycloakUtil.getKeycloakInstance().realm(realm).users()
                .get(keycloakUser.getId()).roles().realmLevel().listAll();
        return checkRoleExists(role, roleRepresentations);
    }

    @Override
    public String createTrainer(String email) {
        UserRepresentation keycloakUser = keycloakUtil.getKeycloakInstance().realm(realm)
                .users()
                .search(email)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntryNotFoundException("User not found! Contact support for assistance"));

        List<RoleRepresentation> roleRepresentations = keycloakUtil.getKeycloakInstance().realm(realm).users()
                .get(keycloakUser.getId()).roles().realmLevel().listAll();

        if (checkRoleExists("trainer", roleRepresentations)) {
            return keycloakUser.getId();
        }
        // Get the representation of the "trainer" role
        RoleRepresentation trainerRole;
        try {
            trainerRole = keycloakUtil.getKeycloakInstance().realm(realm).roles().get("trainer").toRepresentation();
        } catch (Exception e) {
            throw new EntryNotFoundException("User role was not found");
        }
        // Add the "trainer" role to the user
        keycloakUtil.getKeycloakInstance().realm(realm).users()
                .get(keycloakUser.getId()).roles().realmLevel().add(Arrays.asList(trainerRole));
        return keycloakUser.getId();
    }

    @Override
    public String createStudent(String email) {

        boolean isExists = verifyStudent(email);
        if(!isExists) {
            UserRepresentation keycloakUser = keycloakUtil.getKeycloakInstance().realm(realm)
                    .users()
                    .search(email)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EntryNotFoundException("User not found! Contact support for assistance"));

            List<RoleRepresentation> roleRepresentations = keycloakUtil.getKeycloakInstance().realm(realm).users()
                    .get(keycloakUser.getId()).roles().realmLevel().listAll();

            if (checkRoleExists("student", roleRepresentations)) {
                return keycloakUser.getId();
            }
            // Get the representation of the "trainer" role
            RoleRepresentation studentRole;
            try {
                studentRole = keycloakUtil.getKeycloakInstance().realm(realm).roles().get("student").toRepresentation();
            } catch (Exception e) {
                throw new EntryNotFoundException("User role was not found");
            }
            // Add the "trainer" role to the user
            keycloakUtil.getKeycloakInstance().realm(realm).users()
                    .get(keycloakUser.getId()).roles().realmLevel().add(Arrays.asList(studentRole));
            return keycloakUser.getId();
        }else{
            return null;
        }

    }

    @Override
    public void removeTrainer(String email) {
        UserRepresentation keycloakUser = keycloakUtil.getKeycloakInstance().realm(realm)
                .users()
                .search(email)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntryNotFoundException("User not found! Contact support for assistance"));

        List<RoleRepresentation> roleRepresentations = keycloakUtil.getKeycloakInstance().realm(realm).users()
                .get(keycloakUser.getId()).roles().realmLevel().listAll();

        // Check if the user has the "trainer" role
        boolean isTrainer = checkRoleExists("trainer", roleRepresentations);

        if (!isTrainer) {
            throw new EntryNotFoundException("This user is not a trainer.");
        }
        // Get the representation of the "trainer" role
        RoleRepresentation trainerRole;
        try {
            trainerRole = keycloakUtil.getKeycloakInstance().realm(realm).roles().get("trainer").toRepresentation();
        } catch (Exception e) {
            throw new EntryNotFoundException("User role was not found");
        }
        // Add the "trainer" role to the user
        keycloakUtil.getKeycloakInstance().realm(realm).users()
                .get(keycloakUser.getId()).roles().realmLevel().remove(Arrays.asList(trainerRole));
    }

    @Override
    public ResponseUserDetailsDto getUserDetails(String email) {
        Optional<SystemUser> byEmail = systemUserRepo.findByEmail(email);
        if (byEmail.isEmpty()) {
            throw new EntryNotFoundException("User was not found");
        }

        SystemUserAvatar systemUserAvatar = byEmail.get().getSystemUserAvatar();

        return ResponseUserDetailsDto.builder()
                .email(byEmail.get().getEmail())
                .firstName(byEmail.get().getFirstName())
                .lastName(byEmail.get().getLastName())
                .resourceUrl(systemUserAvatar != null ? fileDataExtractor.byteArrayToString(systemUserAvatar.getResourceUrl()) : null)
                .build();
    }

    @Override
    public List<ResponseUserDto> findUsersPaginate(String searchText, int page, int size) {
        try {
            Keycloak keycloak = keycloakUtil.getKeycloakInstance();

            // Calculate the first result index based on the page and size
            int firstResult = page * size;

            // Search for users by email using Keycloak's user search
            List<UserRepresentation> users = keycloak.realm(realm).users()
                    .search(searchText, firstResult, size);

            // Map the retrieved UserRepresentation objects to ResponseUserDto objects
            List<ResponseUserDto> responseUserDtos = new ArrayList<>();
            for (UserRepresentation user : users) {
                ResponseUserDto responseUserDto = mapToResponseUserDto(user);
                responseUserDtos.add(responseUserDto);
            }
            return responseUserDtos;
        } catch (Exception e) {
            // Handle any exceptions
            throw new RuntimeException("Error occurred while searching users", e);
        }

    }


    private ResponseUserDto mapToResponseUserDto(UserRepresentation user) {
        return ResponseUserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();

    }

    private boolean checkRoleExists(String role, List<RoleRepresentation> list) {
        for (RequestRoleDto requestRole : mapRoles(list)
        ) {
            if (requestRole.getName().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public List<RequestRoleDto> mapRoles(List<RoleRepresentation> representations) {
        List<RequestRoleDto> roles = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(representations)) {
            representations.forEach(roleRep -> roles.add(mapRole(roleRep)));
        }
        return roles;
    }

    public RequestRoleDto mapRole(RoleRepresentation roleRep) {
        RequestRoleDto role = new RequestRoleDto();
        role.setId(roleRep.getId());
        role.setName(roleRep.getName());
        return role;
    }

    private UserRepresentation mapUserRep(RequestUserDto user) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(user.getEmail());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEmail(user.getEmail());
        userRep.setEnabled(false);
        userRep.setEmailVerified(false);
        List<CredentialRepresentation> creds = new ArrayList<>();
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false);
        cred.setValue(user.getPassword());
        creds.add(cred);
        userRep.setCredentials(creds);
        return userRep;
    }

}
