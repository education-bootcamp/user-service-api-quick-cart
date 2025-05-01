package com.devstack.quickcart.user_service_api.service.impl;

import com.devstack.quickcart.user_service_api.config.KeycloakSecurityUtil;
import com.devstack.quickcart.user_service_api.dto.request.RequestUserDto;
import com.devstack.quickcart.user_service_api.dto.request.RequestUserLoginRequest;
import com.devstack.quickcart.user_service_api.dto.request.RequestUserPasswordResetDto;
import com.devstack.quickcart.user_service_api.dto.response.ResponseUserDto;
import com.devstack.quickcart.user_service_api.entity.Otp;
import com.devstack.quickcart.user_service_api.entity.User;
import com.devstack.quickcart.user_service_api.exception.*;
import com.devstack.quickcart.user_service_api.repo.OtpRepo;
import com.devstack.quickcart.user_service_api.repo.UserRepo;
import com.devstack.quickcart.user_service_api.service.EmailService;
import com.devstack.quickcart.user_service_api.service.UserService;
import com.devstack.quickcart.user_service_api.util.FileDataExtractor;
import com.devstack.quickcart.user_service_api.util.OtpGenerator;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakSecurityUtil keycloakUtil;

    private final UserRepo systemUserRepo;
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
        // Check if user already exists
        existingUser = keycloak.realm(realm).users().search(dto.getUsername()).stream()
                .findFirst().orElse(null);

        if (existingUser != null) {
            Optional<User> byEmail = systemUserRepo.findByUsername(existingUser.getEmail());
            if (byEmail.isEmpty()) {
                keycloak.realm(realm).users().delete(existingUser.getId());
            } else {
                throw new DuplicateEntryException("User with email " + dto.getUsername() + " already exists.");
            }

        } else {
            Optional<User> byEmail = systemUserRepo.findByUsername(dto.getUsername());
            if (byEmail.isPresent()) {
                Optional<Otp> bySystemUserId = otpRepo.findBySystemUserId(byEmail.get().getUserId());
                if (bySystemUserId.isPresent()) {
                    otpRepo.deleteById(bySystemUserId.get().getPropertyId());
                }
                systemUserRepo.deleteById(byEmail.get().getUserId());
            }
        }

        UserRepresentation userRep = mapUserRep(dto);
        Response res = keycloak.realm(realm).users().create(userRep);
        // Add the admin role to the newly created user
        if (res.getStatus() == Response.Status.CREATED.getStatusCode()) {
            RoleRepresentation userRole = keycloak.realm(realm).roles().get("user").toRepresentation();
            userId = res.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Arrays.asList(userRole));
            User createdSystemUser = User.builder()
                    .userId(userId)
                    .activeState(false)
                    .username(dto.getUsername())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .isAccountNonExpired(true)
                    .isEmailVerified(false)
                    .isAccountNonLocked(true)
                    .isEnabled(false)
                    .createdDate(new Date())
                    .build();
            User savedUser = systemUserRepo.save(createdSystemUser);
            Otp otp = Otp.builder()
                    .propertyId(UUID.randomUUID().toString())
                    .code(otpGenerator.generateOtp(4))
                    .createdDate(new Date())
                    .isVerified(false)
                    .attempts(0)
                    .systemUser(savedUser)
                    .build();
            otpRepo.save(otp);
            emailService.sendUserSignupVerificationCode(dto.getUsername(),
                    "Verify Your Email Address for Developers Stack Access", otp.getCode());
        }
    }

    @Override
    public boolean verifyEmail(String otp, String email) {
        try {
            Optional<User> selectedUserObj = systemUserRepo.findByUsername(email);
            if (selectedUserObj.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
            }
            User systemUser = selectedUserObj.get();

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
            Optional<User> selectedUserObj = systemUserRepo.findByUsername(request.getUsername());
            User systemUser = selectedUserObj.get();
            if (!systemUser.getIsEmailVerified()) {

                Otp selectedOtpObj = systemUser.getOtp();
                if (selectedOtpObj.getAttempts() >= 5) {

                    String code = otpGenerator.generateOtp(4);

                    emailService.sendUserSignupVerificationCode(systemUser.getUsername(),
                            "Verify Your Email Address for Developers Stack Access", code);

                    selectedOtpObj.setAttempts(0);
                    selectedOtpObj.setCode(code);
                    selectedOtpObj.setCreatedDate(new Date());
                    otpRepo.save(selectedOtpObj);

                    throw new TooManyRequestException("Too many unsuccessful attempts. New OTP sent and please verify.");
                }
                emailService.sendUserSignupVerificationCode(systemUser.getFirstName(),
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

    @Override
    public void resend(String email) {
        try {
            Optional<User> selectedUserObj = systemUserRepo.findByUsername(email);
            if (selectedUserObj.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
            }
            User systemUser = selectedUserObj.get();
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
            emailService.sendUserSignupVerificationCode(systemUser.getUsername(),
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
        Optional<User> selectedUserObj = systemUserRepo.findByUsername(email);
        if (selectedUserObj.isEmpty()) {
            throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
        }
        User systemUser = selectedUserObj.get();
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
            emailService.sendPasswordResetVerificationCode(systemUser.getUsername(),
                    "Verify Your Email Address for Developers Stack Access", selectedOtpObj.getCode());
        } catch (IOException e) {
            throw new UnauthorizedException("Invalid username or password. Please double-check your credentials and try again.");

        }

    }

    @Override
    public boolean verifyReset(String otp, String email) {
        try {
            Optional<User> selectedUserObj = systemUserRepo.findByUsername(email);
            if (selectedUserObj.isEmpty()) {
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address.");
            }
            User systemUser = selectedUserObj.get();
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
        Optional<User> selectedUserObj = systemUserRepo.findByUsername(dto.getEmail());
        if (selectedUserObj.isPresent()) {
            User systemUser = selectedUserObj.get();
            Otp selectedOtpObj = systemUser.getOtp();
            Keycloak keycloak = keycloakUtil.getKeycloakInstance();
            List<UserRepresentation> keycloakUsers = keycloak.realm(realm).users().search(systemUser.getUsername());
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
    public String getUserId(String email) {
        Optional<User> byEmail = systemUserRepo.findByUsername(email);
        if (byEmail.isEmpty()) {
            throw new EntryNotFoundException("User was not found");
        }
        return byEmail.get().getUserId();
    }

    private UserRepresentation mapUserRep(RequestUserDto user) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(user.getUsername());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEmail(user.getUsername());
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

    private ResponseUserDto mapToResponseUserDto(UserRepresentation user) {
        return ResponseUserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getEmail())
                .username(user.getUsername())
                .avatar(null)
                .billingAddress(null)
                .shippingAddress(null)
                .build();

    }


}
