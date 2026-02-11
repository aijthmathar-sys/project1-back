package com.example.project1.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.project1.dto.LoginRequest;
import com.example.project1.dto.LoginResponse;
import com.example.project1.dto.SignupRequest;
import com.example.project1.dto.UserResponse;
import com.example.project1.module.User;
import com.example.project1.repositary.UserRepositary;
import com.example.project1.security.JwUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositary userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setPhoneNumber("+14155552671");
        signupRequest.setRole("JOB_SEEKER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("JOB_SEEKER");
        testUser.setPhoneNumber("+14155552671");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testSignupSuccess() {
        // Arrange
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = authService.signup(signupRequest);

        // Assert
        assertEquals("Signup Success", result);
        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(signupRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignupEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // Act
        String result = authService.signup(signupRequest);

        // Assert
        assertEquals("Email already exists", result);
        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSignupWithRolePrefix() {
        // Arrange
        signupRequest.setRole("ROLE_EMPLOYER");
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = authService.signup(signupRequest);

        // Assert
        assertEquals("Signup Success", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateTokenWithRole(testUser.getEmail(), testUser.getRole())).thenReturn("test_jwt_token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test_jwt_token", response.getToken());
        assertEquals("JOB_SEEKER", response.getRole());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    void testLoginUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNull(response.getToken());
        assertNull(response.getRole());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    }

    @Test
    void testLoginWrongPassword() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNull(response.getToken());
        assertNull(response.getRole());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    void testGetUserInfoSuccess() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = authService.getUserInfo(testUser.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getPhoneNumber(), response.getPhoneNumber());
    }

    @Test
    void testGetUserInfoNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        UserResponse response = authService.getUserInfo("nonexistent@example.com");

        // Assert
        assertNull(response);
    }
}
