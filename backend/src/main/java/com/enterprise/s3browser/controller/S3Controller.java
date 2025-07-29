package com.enterprise.s3browser.controller;

import com.enterprise.s3browser.dto.S3ObjectDto;
import com.enterprise.s3browser.model.S3Object;
import com.enterprise.s3browser.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for S3 operations.
 */
@RestController
@RequestMapping("/api/s3")
@Tag(name = "S3 Operations", description = "S3 storage operations")
@SecurityRequirement(name = "bearerAuth")
public class S3Controller {

    private static final Logger logger = LoggerFactory.getLogger(S3Controller.class);

    @Autowired
    private S3Service s3Service;

    @Operation(summary = "List objects", description = "List objects in the bucket with optional prefix")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved objects")
    @GetMapping("/objects")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<S3ObjectDto>> listObjects(
            @Parameter(description = "Prefix to filter objects") @RequestParam(required = false) String prefix) {
        
        logger.info("Listing objects with prefix: {}", prefix);
        
        List<S3Object> objects = s3Service.listObjects(prefix);
        List<S3ObjectDto> objectDtos = objects.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(objectDtos);
    }

    @Operation(summary = "Get object metadata", description = "Get metadata for a specific object")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved object metadata")
    @GetMapping("/objects/{key}/metadata")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<S3ObjectDto> getObjectMetadata(
            @Parameter(description = "Object key") @PathVariable String key) {
        
        logger.info("Getting metadata for object: {}", key);
        
        S3Object object = s3Service.getObjectMetadata(key);
        S3ObjectDto objectDto = convertToDto(object);
        
        return ResponseEntity.ok(objectDto);
    }

    @Operation(summary = "Download object", description = "Download object content")
    @ApiResponse(responseCode = "200", description = "Successfully downloaded object")
    @GetMapping("/objects/download")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ByteArrayResource> downloadObject(
            @Parameter(description = "Object key") @RequestParam String key) {
        
        logger.info("Downloading object: {}", key);
        
        ResponseBytes<GetObjectResponse> response = s3Service.downloadObject(key);
        ByteArrayResource resource = new ByteArrayResource(response.asByteArray());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                   "attachment; filename=\"" + URLEncoder.encode(getFileName(key), StandardCharsets.UTF_8) + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, 
                   response.response().contentType() != null ? response.response().contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(response.asByteArray().length)
                .body(resource);
    }

    @Operation(summary = "Upload object", description = "Upload a file to S3")
    @ApiResponse(responseCode = "201", description = "Successfully uploaded object")
    @PostMapping("/objects")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<String> uploadObject(
            @Parameter(description = "Object key") @RequestParam String key,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file) throws IOException {
        
        logger.info("Uploading object: {} (size: {} bytes)", key, file.getSize());
        
        s3Service.uploadObject(key, file.getInputStream(), file.getSize(), file.getContentType());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Object uploaded successfully: " + key);
    }

    @Operation(summary = "Delete object", description = "Delete an object from S3")
    @ApiResponse(responseCode = "204", description = "Successfully deleted object")
    @DeleteMapping("/objects")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Void> deleteObject(
            @Parameter(description = "Object key") @RequestParam String key) {
        
        logger.info("Deleting object: {}", key);
        
        s3Service.deleteObject(key);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List buckets", description = "List all available buckets")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved buckets")
    @GetMapping("/buckets")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<String>> listBuckets() {
        
        logger.info("Listing buckets");
        
        List<String> buckets = s3Service.listBuckets();
        
        return ResponseEntity.ok(buckets);
    }

    @Operation(summary = "Create folder", description = "Create a new folder in S3")
    @ApiResponse(responseCode = "201", description = "Successfully created folder")
    @PostMapping("/folders")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<String> createFolder(
            @Parameter(description = "Folder path") @RequestParam String folderPath) {
        
        logger.info("Creating folder: {}", folderPath);
        
        s3Service.createFolder(folderPath);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Folder created successfully: " + folderPath);
    }

    @Operation(summary = "Test connection", description = "Test S3 connection")
    @ApiResponse(responseCode = "200", description = "Connection successful")
    @GetMapping("/test-connection")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<String> testConnection() {
        
        logger.info("Testing S3 connection");
        
        s3Service.testConnection();
        
        return ResponseEntity.ok("Connection successful");
    }

    private S3ObjectDto convertToDto(S3Object object) {
        S3ObjectDto dto = new S3ObjectDto();
        dto.setKey(object.getKey());
        dto.setEtag(object.getEtag());
        dto.setSize(object.getSize());
        dto.setLastModified(object.getLastModified());
        dto.setStorageClass(object.getStorageClass());
        dto.setDirectory(object.isDirectory());
        return dto;
    }

    private String getFileName(String key) {
        if (key.contains("/")) {
            return key.substring(key.lastIndexOf("/") + 1);
        }
        return key;
    }
}