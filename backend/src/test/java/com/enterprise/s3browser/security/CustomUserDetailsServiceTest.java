package com.enterprise.s3browser.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(customUserDetailsService, "adminUsername", "admin");
        ReflectionTestUtils.setField(customUserDetailsService, "adminPassword", "admin123");
    }

    @Test
    void loadUserByUsername_ValidUser() {
        when(passwordEncoder.encode("admin123")).thenReturn("encoded_password");

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsername_InvalidUser() {
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("invaliduser");
        });
    }

    @Test
    void loadUserByUsername_NullUsername() {
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(null);
        });
    }

    @Test
    void loadUserByUsername_EmptyUsername() {
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("");
        });
    }

    @Test
    void validateCredentials_ValidCredentials() {
        boolean isValid = customUserDetailsService.validateCredentials("admin", "admin123");
        
        assertTrue(isValid);
    }

    @Test
    void validateCredentials_InvalidUsername() {
        boolean isValid = customUserDetailsService.validateCredentials("wronguser", "admin123");
        
        assertFalse(isValid);
    }

    @Test
    void validateCredentials_InvalidPassword() {
        boolean isValid = customUserDetailsService.validateCredentials("admin", "wrongpassword");
        
        assertFalse(isValid);
    }

    @Test
    void validateCredentials_BothInvalid() {
        boolean isValid = customUserDetailsService.validateCredentials("wronguser", "wrongpassword");
        
        assertFalse(isValid);
    }

    @Test
    void validateCredentials_NullCredentials() {
        boolean isValid = customUserDetailsService.validateCredentials(null, null);
        
        assertFalse(isValid);
    }
}