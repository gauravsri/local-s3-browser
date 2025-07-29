package com.enterprise.s3browser.config;

import com.enterprise.s3browser.model.S3Configuration;
import com.enterprise.s3browser.service.S3ConfigurationService;
import com.enterprise.s3browser.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * S3 configuration initialization.
 */
@Configuration
public class S3Config implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(S3Config.class);

    @Autowired
    private S3ConfigurationService configurationService;

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

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing S3 configuration...");
        
        try {
            // Create default configuration from application.yml
            S3Configuration defaultConfig = new S3Configuration();
            defaultConfig.setEndpoint(defaultEndpoint);
            defaultConfig.setAccessKey(defaultAccessKey);
            defaultConfig.setSecretKey(defaultSecretKey);
            defaultConfig.setBucket(defaultBucket);
            defaultConfig.setRegion(defaultRegion);
            defaultConfig.setPathStyleAccess(defaultPathStyleAccess);

            // Set as current configuration
            configurationService.updateConfiguration(defaultConfig);
            
            // Initialize S3 client
            s3Service.initializeClient(defaultConfig);
            
            logger.info("S3 configuration initialized successfully with endpoint: {}", defaultEndpoint);
            
        } catch (Exception e) {
            logger.warn("Failed to initialize S3 configuration with default settings: {}", e.getMessage());
            logger.info("S3 configuration can be updated via the /api/config endpoint");
        }
    }
}