package com.enterprise.s3browser.controller;

import com.enterprise.s3browser.dto.S3ConfigurationDto;
import com.enterprise.s3browser.model.S3Configuration;
import com.enterprise.s3browser.service.S3ConfigurationService;
import com.enterprise.s3browser.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for S3 configuration operations.
 */
@RestController
@RequestMapping("/api/config")
@Tag(name = "Configuration", description = "S3 configuration operations")
@SecurityRequirement(name = "bearerAuth")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private S3ConfigurationService configurationService;

    @Autowired
    private S3Service s3Service;

    @Operation(summary = "Get current configuration", description = "Get current S3 configuration")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved configuration")
    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<S3ConfigurationDto> getCurrentConfiguration() {
        
        logger.info("Getting current S3 configuration");
        
        S3Configuration config = configurationService.getCurrentConfiguration();
        S3ConfigurationDto configDto = convertToDto(config);
        
        return ResponseEntity.ok(configDto);
    }

    @Operation(summary = "Update configuration", description = "Update S3 configuration")
    @ApiResponse(responseCode = "200", description = "Successfully updated configuration")
    @ApiResponse(responseCode = "400", description = "Invalid configuration")
    @PutMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<String> updateConfiguration(@Valid @RequestBody S3ConfigurationDto configDto) {
        
        logger.info("Updating S3 configuration with endpoint: {}", configDto.getEndpoint());
        
        S3Configuration config = convertFromDto(configDto);
        configurationService.updateConfiguration(config);
        
        s3Service.initializeClient(config);
        
        logger.info("S3 configuration updated successfully");
        
        return ResponseEntity.ok("Configuration updated successfully");
    }

    @Operation(summary = "Test configuration", description = "Test S3 configuration without saving")
    @ApiResponse(responseCode = "200", description = "Configuration is valid")
    @ApiResponse(responseCode = "400", description = "Configuration is invalid")
    @PostMapping("/test")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<String> testConfiguration(@Valid @RequestBody S3ConfigurationDto configDto) {
        
        logger.info("Testing S3 configuration with endpoint: {}", configDto.getEndpoint());
        
        S3Configuration config = convertFromDto(configDto);
        
        try {
            configurationService.testConfiguration(config);
            return ResponseEntity.ok("Configuration is valid and connection successful");
        } catch (Exception e) {
            logger.error("Configuration test failed", e);
            return ResponseEntity.badRequest().body("Configuration test failed: " + e.getMessage());
        }
    }

    private S3ConfigurationDto convertToDto(S3Configuration config) {
        S3ConfigurationDto dto = new S3ConfigurationDto();
        dto.setEndpoint(config.getEndpoint());
        dto.setAccessKey(config.getAccessKey());
        dto.setSecretKey(maskSecretKey(config.getSecretKey()));
        dto.setBucket(config.getBucket());
        dto.setRegion(config.getRegion());
        dto.setPathStyleAccess(config.isPathStyleAccess());
        return dto;
    }

    private S3Configuration convertFromDto(S3ConfigurationDto dto) {
        S3Configuration config = new S3Configuration();
        config.setEndpoint(dto.getEndpoint());
        config.setAccessKey(dto.getAccessKey());
        config.setSecretKey(dto.getSecretKey());
        config.setBucket(dto.getBucket());
        config.setRegion(dto.getRegion());
        config.setPathStyleAccess(dto.getPathStyleAccess());
        return config;
    }

    private String maskSecretKey(String secretKey) {
        if (secretKey == null || secretKey.length() <= 4) {
            return "****";
        }
        return secretKey.substring(0, 4) + "****";
    }
}