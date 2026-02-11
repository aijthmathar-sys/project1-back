package com.example.project1.service;

import com.example.project1.dto.LoginRequest;
import com.example.project1.dto.LoginResponse;
import com.example.project1.dto.SignupRequest;
import com.example.project1.dto.UserResponse;
import com.example.project1.module.User;
import com.example.project1.repositary.UserRepositary;
import com.example.project1.security.JwUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepositary userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwUtil jwtUtil;

    // ---------- SIGNUP ----------
    public String signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }

        String role = request.getRole() == null ? "JOB_SEEKER" : request.getRole().toUpperCase();
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        User user = new User(
            null,
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            role,
            request.getPhoneNumber()
        );

        userRepository.save(user);
        return "Signup Success";
    }

    // ---------- LOGIN ----------
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return new LoginResponse(null, null);
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            return new LoginResponse(null, null);
        }

        String token = jwtUtil.generateTokenWithRole(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getRole());
    }

    public UserResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber());
    }
}