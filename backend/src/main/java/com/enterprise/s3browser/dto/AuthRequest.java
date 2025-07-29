package com.enterprise.s3browser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for authentication requests.
 */
@Schema(description = "Authentication request")
public class AuthRequest {

    @Schema(description = "Username", example = "admin", required = true)
    @JsonProperty("username")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Schema(description = "Password", example = "password", required = true)
    @JsonProperty("password")
    @NotBlank(message = "Password cannot be blank")
    private String password;

    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}