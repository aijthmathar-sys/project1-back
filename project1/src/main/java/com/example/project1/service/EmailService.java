package com.example.project1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Simple;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.twilio.rest.preview.wireless.Sim;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp+"\nThis code will expire in 5 minutes.");
        mailSender.send(message);
    }
    }
