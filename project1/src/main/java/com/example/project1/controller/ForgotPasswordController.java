package com.example.project1.controller;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.project1.module.OtpEntity;
import com.example.project1.module.User;
import com.example.project1.repositary.UserRepositary;
import com.example.project1.service.EmailService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/auth")
public class ForgotPasswordController {

    @Autowired
    private UserRepositary userRepository;

    @Autowired
    private com.example.project1.repositary.OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ============================
    // STEP 1️⃣ : SEND OTP
    // ============================
    @Transactional
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // generate 6 digit OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        // delete old otp if exists
        otpRepository.deleteByEmail(email);

        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpEntity);

        emailService.sendOtp(email, otp);

        return ResponseEntity.ok("OTP sent successfully");
    }

    // ============================
    // STEP 2️⃣ : VERIFY OTP
    // ============================
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestBody String email,
            String otp) {

        Optional<OtpEntity> otpOpt = otpRepository.findByEmail(email);

        if (otpOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("OTP not found");
        }

        OtpEntity otpEntity = otpOpt.get();

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP expired");
        }

        if (!otpEntity.getOtp().equals(otp)) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        return ResponseEntity.ok("OTP verified successfully");
    }

    // ============================
    // STEP 3️⃣ : RESET PASSWORD
    // ============================
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {

        Optional<OtpEntity> otpOpt = otpRepository.findByEmail(email);
        if (otpOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("OTP not found");
        }

        OtpEntity otpEntity = otpOpt.get();

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP expired");
        }

        if (!otpEntity.getOtp().equals(otp)) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        User user = userRepository.findByEmail(email).get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpRepository.deleteByEmail(email);

        return ResponseEntity.ok("Password reset successful");
    }
}
