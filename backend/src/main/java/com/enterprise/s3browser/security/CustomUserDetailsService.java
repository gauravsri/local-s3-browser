package com.enterprise.s3browser.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Custom user details service for authentication.
 * In this simple implementation, we have a single hardcoded user.
 * For enterprise use, this would typically integrate with LDAP, database, or other user stores.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Value("${app.auth.username:admin}")
    private String adminUsername;

    @Value("${app.auth.password:admin}")
    private String adminPassword;

    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (adminUsername.equals(username)) {
            return UserPrincipal.create(username, passwordEncoder.encode(adminPassword));
        }
        
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    /**
     * Validate user credentials.
     */
    public boolean validateCredentials(String username, String password) {
        if (!adminUsername.equals(username)) {
            return false;
        }
        
        return adminPassword.equals(password);
    }
}