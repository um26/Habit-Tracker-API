package com.habittracker.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.sender}")
    private String senderEmail;

    // Inject the base URL from application.properties
    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String recipientEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(recipientEmail);
            message.setSubject("Habit Tracker - Verify Your Email");

            // Construct the URL using the base URL property
            String confirmationUrl = baseUrl + "/verify-email?token=" + token;

            message.setText("Thank you for registering! Please click the link below to verify your email address:\n" + confirmationUrl);
            mailSender.send(message);
            log.info("Verification email sent successfully to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Error sending verification email to {}: {}", recipientEmail, e.getMessage());
        }
    }
}