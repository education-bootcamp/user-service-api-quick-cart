package com.devstack.system.service.impl;

import com.devstack.system.service.EmailService;
import com.devstack.system.util.EmailTemplateHelper;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailTemplateHelper emailTemplateHelper;

    @Value("${fromEmail}")
    private String senderEmail;

    @Value("${emailKey}")
    private String apiKey;

    @Override
    public boolean sendUserSignupVerificationCode(String toEmail, String subject, String otp) throws IOException {
        String htmlBody = emailTemplateHelper.loadHtmlTemplate("templates/dev-stack-send-login-verification-email-template.html");
        htmlBody = htmlBody.replace("${otp}", otp);
        htmlBody = htmlBody.replace("${year}", String.valueOf(Year.now().getValue()));

        Email from = new Email(senderEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
        } catch (IOException ex) {
            System.out.println(ex);
            throw ex;
        }
        return true;
    }

    @Override
    public boolean sendPasswordResetVerificationCode(String toEmail, String subject, String otp) throws IOException {
        String htmlBody = emailTemplateHelper.loadHtmlTemplate("templates/dev-stack-send-reset-password-verification-email-template.html");
        htmlBody = htmlBody.replace("${otp}", otp);
        htmlBody = htmlBody.replace("${year}", String.valueOf(Year.now().getValue()));

        Email from = new Email(senderEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
        } catch (IOException ex) {
            System.out.println(ex);
            throw ex;
        }
        return true;
    }
}
