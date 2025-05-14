package com.example.healthflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;
    
    @Value("${spring.mail.username:luhmeh1221@gmail.com}")
    private String fromAddress;
    
    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
    
    @Override
    public void sendAccountCredentials(String to, String username, String password) {
        String subject = "Welcome to HealthFlow - Your Account Credentials";
        String text = "Dear Patient,\n\n" +
                "Thank you for booking an appointment with HealthFlow.\n\n" +
                "We have created an account for you to manage your appointments. " +
                "Here are your login credentials:\n\n" +
                "Username: " + username + "\n" +
                "Password: " + password + "\n\n" +
                "Please login at https://healthflow.com/login to manage your appointments and access your health records.\n\n" +
                "For security reasons, we recommend changing your password after your first login.\n\n" +
                "If you have any questions, please contact our support team.\n\n" +
                "Best regards,\n" +
                "The HealthFlow Team";
        
        sendSimpleMessage(to, subject, text);
    }
} 