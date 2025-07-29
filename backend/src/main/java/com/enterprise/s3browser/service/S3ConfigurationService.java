package com.enterprise.s3browser.service;

import com.enterprise.s3browser.model.S3Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for managing S3 configuration dynamically.
 * Handles configuration updates and applies them to the S3 service.
 */
@Service
public class S3ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(S3ConfigurationService.class);

    @Autowired
    private S3Service s3Service;

    @Value("${s3.endpoint}")
    private String defaultEndpoint;

    @Value("${s3.access-key}")
    private String defaultAccessKey;

    @Value("${s3.secret-key}")
    private String defaultSecretKey;

    @Value("${s3.bucket}")
    private String defaultBucket;

    @Value("${s3.region}")
    private String defaultRegion;

    @Value("${s3.path-style-access}")
    private boolean defaultPathStyleAccess;

    /**
     * Initialize S3 service with default configuration on startup.
     */
    public void initializeWithDefaults() {
        try {
            S3Configuration defaultConfig = new S3Configuration(
                defaultEndpoint,
                defaultAccessKey,
                defaultSecretKey,
                defaultBucket,
                defaultRegion,
                defaultPathStyleAccess
            );

            logger.info("Initializing S3 service with default configuration");
            s3Service.initializeClient(defaultConfig);
            
        } catch (Exception e) {
            logger.error("Failed to initialize S3 service with default configuration", e);
            // Don't throw exception to allow app to start even if MinIO is not available
        }
    }

    /**
     * Update S3 configuration and reinitialize the client.
     */
    public void updateConfiguration(S3Configuration newConfig) {
        logger.info("Updating S3 configuration to endpoint: {}", newConfig.getEndpoint());
        
        // Validate configuration
        validateConfiguration(newConfig);
        
        // Initialize S3 client with new configuration
        s3Service.initializeClient(newConfig);
        
        logger.info("S3 configuration updated successfully");
    }

    /**
     * Get current S3 configuration (with secrets masked for security).
     */
    public S3Configuration getCurrentConfiguration() {
        S3Configuration current = s3Service.getCurrentConfig();
        
        if (current == null) {
            // Return default configuration if no current config
            return new S3Configuration(
                defaultEndpoint,
                defaultAccessKey,
                "***MASKED***", // Mask secret for security
                defaultBucket,
                defaultRegion,
                defaultPathStyleAccess
            );
        }
        
        // Create a copy with masked secret key
        return new S3Configuration(
            current.getEndpoint(),
            current.getAccessKey(),
            "***MASKED***", // Mask secret for security
            current.getBucket(),
            current.getRegion(),
            current.isPathStyleAccess()
        );
    }

    /**
     * Get current configuration with full details (including secrets).
     * Should only be used internally, never exposed via API.
     */
    public S3Configuration getCurrentConfigurationInternal() {
        S3Configuration current = s3Service.getCurrentConfig();
        
        if (current == null) {
            return new S3Configuration(
                defaultEndpoint,
                defaultAccessKey,
                defaultSecretKey,
                defaultBucket,
                defaultRegion,
                defaultPathStyleAccess
            );
        }
        
        return current;
    }

    /**
     * Test connection with current configuration.
     */
    public void testCurrentConnection() {
        s3Service.testConnection();
    }

    /**
     * Test connection with provided configuration without applying it.
     */
    public void testConfiguration(S3Configuration config) {
        // Create a temporary S3 service instance for testing
        S3Service tempService = new S3Service();
        tempService.initializeClient(config);
        tempService.testConnection();
    }

    /**
     * Validate S3 configuration parameters.
     */
    private void validateConfiguration(S3Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
        if (config.getEndpoint() == null || config.getEndpoint().trim().isEmpty()) {
            throw new IllegalArgumentException("Endpoint cannot be null or empty");
        }
        
        if (config.getAccessKey() == null || config.getAccessKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Access key cannot be null or empty");
        }
        
        if (config.getSecretKey() == null || config.getSecretKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        
        if (config.getBucket() == null || config.getBucket().trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket cannot be null or empty");
        }
        
        if (config.getRegion() == null || config.getRegion().trim().isEmpty()) {
            throw new IllegalArgumentException("Region cannot be null or empty");
        }
        
        // Validate endpoint format
        try {
            java.net.URI.create(config.getEndpoint());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid endpoint URL format: " + config.getEndpoint());
        }
    }

    /**
     * Check if S3 service is initialized.
     */
    public boolean isInitialized() {
        return s3Service.isInitialized();
    }
}