package com.devstack.system.service;

import java.io.IOException;

public interface EmailService {
    public boolean sendUserSignupVerificationCode(String toEmail, String subject, String otp) throws IOException;
    public boolean sendPasswordResetVerificationCode(String toEmail, String subject, String otp) throws IOException;


}
