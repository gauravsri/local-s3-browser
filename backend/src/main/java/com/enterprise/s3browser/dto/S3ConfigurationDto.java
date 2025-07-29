package com.enterprise.s3browser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for S3 configuration updates.
 */
@Schema(description = "S3 configuration settings")
public class S3ConfigurationDto {

    @Schema(description = "S3 endpoint URL", example = "http://localhost:9000", required = true)
    @JsonProperty("endpoint")
    @NotBlank(message = "Endpoint cannot be blank")
    private String endpoint;

    @Schema(description = "S3 access key", example = "minioadmin", required = true)
    @JsonProperty("accessKey")
    @NotBlank(message = "Access key cannot be blank")
    private String accessKey;

    @Schema(description = "S3 secret key", example = "minioadmin", required = true)
    @JsonProperty("secretKey")
    @NotBlank(message = "Secret key cannot be blank")
    private String secretKey;

    @Schema(description = "S3 bucket name", example = "my-bucket", required = true)
    @JsonProperty("bucket")
    @NotBlank(message = "Bucket cannot be blank")
    private String bucket;

    @Schema(description = "S3 region", example = "us-east-1", required = true)
    @JsonProperty("region")
    @NotBlank(message = "Region cannot be blank")
    private String region;

    @Schema(description = "Use path-style access", example = "true", required = true)
    @JsonProperty("pathStyleAccess")
    @NotNull(message = "Path style access setting is required")
    private Boolean pathStyleAccess;

    public S3ConfigurationDto() {}

    public S3ConfigurationDto(String endpoint, String accessKey, String secretKey, 
                             String bucket, String region, Boolean pathStyleAccess) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.region = region;
        this.pathStyleAccess = pathStyleAccess;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Boolean getPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(Boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }
}