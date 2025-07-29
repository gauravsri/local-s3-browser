package com.enterprise.s3browser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for authentication responses.
 */
@Schema(description = "Authentication response")
public class AuthResponse {

    @Schema(description = "JWT access token")
    @JsonProperty("accessToken")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    @JsonProperty("type")
    private String type = "Bearer";

    @Schema(description = "Username")
    @JsonProperty("username")
    private String username;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String username) {
        this.accessToken = accessToken;
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}