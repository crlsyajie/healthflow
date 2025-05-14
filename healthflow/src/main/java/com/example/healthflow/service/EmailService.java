package com.example.healthflow.service;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendAccountCredentials(String to, String username, String password);
} 