package com.devstack.quickcart.user_service_api.service;

import com.devstack.quickcart.user_service_api.dto.request.RequestUserDto;
import com.devstack.quickcart.user_service_api.dto.request.RequestUserLoginRequest;
import com.devstack.quickcart.user_service_api.dto.request.RequestUserPasswordResetDto;
import com.devstack.quickcart.user_service_api.dto.response.ResponseUserDto;

import java.io.IOException;
import java.util.List;

public interface UserService {
    public void createUser(RequestUserDto dto) throws IOException;
    public boolean verifyEmail(String otp, String email);
    public Object userLogin(RequestUserLoginRequest request);
    public List<ResponseUserDto> findUsersPaginate(String searchText, int page, int size);
    public void resend(String email);
    public void forgotPasswordSendVerificationCode(String email);
    public boolean verifyReset(String otp, String email);
    public boolean passwordReset(RequestUserPasswordResetDto dto);
    String getUserId(String email);
}
