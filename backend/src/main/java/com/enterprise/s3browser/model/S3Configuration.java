package com.enterprise.s3browser.model;

/**
 * Domain model for S3 configuration settings.
 */
public class S3Configuration {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
    private boolean pathStyleAccess;

    public S3Configuration() {}

    public S3Configuration(String endpoint, String accessKey, String secretKey, 
                          String bucket, String region, boolean pathStyleAccess) {
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

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }
}