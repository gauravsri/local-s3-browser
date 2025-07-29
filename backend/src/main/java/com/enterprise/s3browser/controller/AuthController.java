package com.enterprise.s3browser.controller;

import com.enterprise.s3browser.dto.AuthRequest;
import com.enterprise.s3browser.dto.AuthResponse;
import com.enterprise.s3browser.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication operations")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Operation(summary = "Authenticate user", description = "Authenticate user and return JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        
        logger.info("Authentication attempt for user: {}", loginRequest.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        
        logger.info("User authenticated successfully: {}", loginRequest.getUsername());
        
        return ResponseEntity.ok(new AuthResponse(jwt, loginRequest.getUsername()));
    }

    @Operation(summary = "Validate token", description = "Validate JWT token")
    @ApiResponse(responseCode = "200", description = "Token is valid")
    @ApiResponse(responseCode = "401", description = "Token is invalid")
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                logger.debug("Token validated for user: {}", username);
                return ResponseEntity.ok("Token is valid for user: " + username);
            }
        }
        
        return ResponseEntity.status(401).body("Invalid token");
    }

    @Operation(summary = "Logout user", description = "Logout user (client-side token removal)")
    @ApiResponse(responseCode = "200", description = "Successfully logged out")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        
        SecurityContextHolder.clearContext();
        logger.info("User logged out successfully");
        
        return ResponseEntity.ok("Logged out successfully");
    }
}