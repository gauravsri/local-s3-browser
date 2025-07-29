package com.enterprise.s3browser.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private Authentication authentication;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "mySecretKeyForTestingPurposesOnly123456789");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 86400000); // 24 hours

        userPrincipal = UserPrincipal.create("testuser", "password");
        authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("USER"))
        );
    }

    @Test
    void generateToken_Success() {
        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void getUsernameFromToken_Success() {
        String token = jwtTokenProvider.generateToken(authentication);
        
        String username = jwtTokenProvider.getUsernameFromToken(token);
        
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_ValidToken() {
        String token = jwtTokenProvider.generateToken(authentication);
        
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken() {
        String invalidToken = "invalid.token.here";
        
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken() {
        boolean isValid = jwtTokenProvider.validateToken("");
        
        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken() {
        boolean isValid = jwtTokenProvider.validateToken(null);
        
        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_InvalidToken() {
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken("invalid.token.here");
        });
    }
}