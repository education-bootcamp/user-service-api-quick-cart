package com.devstack.system.service;

import com.devstack.system.dto.request.RequestUserDto;
import com.devstack.system.dto.request.RequestUserLoginRequest;
import com.devstack.system.dto.request.RequestUserPasswordResetDto;
import com.devstack.system.dto.response.ResponseUserDetailsDto;
import com.devstack.system.dto.response.ResponseUserDto;

import java.io.IOException;
import java.util.List;

public interface UserService {
    public void createUser(RequestUserDto dto) throws IOException;
    public boolean verifyEmail(String otp, String email);
    public Object userLogin(RequestUserLoginRequest request);
    public boolean verifyAdmin(String email);
    public boolean verifyStudent(String email);
    public boolean verifyRole(String email, String role);
    public String createTrainer(String email);
    public String createStudent(String email);

    public void removeTrainer(String email);
    public ResponseUserDetailsDto getUserDetails(String email);
    public List<ResponseUserDto> findUsersPaginate(String searchText, int page, int size);

    public void resend(String email);

    public void forgotPasswordSendVerificationCode(String email);

    public boolean verifyReset(String otp, String email);

    public boolean passwordReset(RequestUserPasswordResetDto dto);

    Long trustedUserCount();

    String getUserId(String email);
}
