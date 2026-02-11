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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.project1.module.User;
import com.example.project1.repositary.UserRepositary;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositary userRepositary;

    @Mock
    private SecurityContext mockSecurityContext;

    @Mock
    private Authentication mockAuthentication;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPhoneNumber("+14155552671");
        testUser.setRole("JOB_SEEKER");
    }

    @Test
    void testGetLoggedInUserSuccess() {
        // Arrange
        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getName()).thenReturn(testUser.getEmail());
        when(userRepositary.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getLoggedInUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());
        verify(userRepositary, times(1)).findByEmail(testUser.getEmail());
    }

    @Test
    void testGetLoggedInUserNotFound() {
        // Arrange
        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepositary.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getLoggedInUser();
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetLoggedInUserMultipleCalls() {
        // Arrange
        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getName()).thenReturn(testUser.getEmail());
        when(userRepositary.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        User result1 = userService.getLoggedInUser();
        User result2 = userService.getLoggedInUser();

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        verify(userRepositary, times(2)).findByEmail(testUser.getEmail());
    }

    @Test
    void testGetLoggedInUserDifferentRoles() {
        // Arrange - Test with EMPLOYER role
        User employer = new User();
        employer.setId(2L);
        employer.setName("Jane Employer");
        employer.setEmail("employer@example.com");
        employer.setRole("EMPLOYER");

        SecurityContextHolder.setContext(mockSecurityContext);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getName()).thenReturn(employer.getEmail());
        when(userRepositary.findByEmail(employer.getEmail())).thenReturn(Optional.of(employer));

        // Act
        User result = userService.getLoggedInUser();

        // Assert
        assertNotNull(result);
        assertEquals("EMPLOYER", result.getRole());
        assertEquals("Jane Employer", result.getName());
    }
}
