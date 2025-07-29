package com.enterprise.s3browser.service;

import com.enterprise.s3browser.exception.S3ServiceException;
import com.enterprise.s3browser.model.S3Configuration;
import com.enterprise.s3browser.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for S3 operations.
 * Handles all interactions with S3-compatible storage.
 */
@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private S3Client s3Client;
    private S3Configuration currentConfig;

    /**
     * Initialize S3 client with given configuration.
     */
    public void initializeClient(S3Configuration config) {
        try {
            logger.info("Initializing S3 client with endpoint: {}", config.getEndpoint());
            
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                config.getAccessKey(), 
                config.getSecretKey()
            );

            S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(config.getRegion()))
                .httpClient(UrlConnectionHttpClient.builder().build());

            if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
                builder.endpointOverride(URI.create(config.getEndpoint()));
            }

            if (config.isPathStyleAccess()) {
                builder.forcePathStyle(true);
            }

            this.s3Client = builder.build();
            this.currentConfig = config;
            
            testConnection();
            logger.info("S3 client initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize S3 client", e);
            throw new S3ServiceException("Failed to initialize S3 client: " + e.getMessage(), e);
        }
    }

    /**
     * Test S3 connection by listing buckets.
     */
    public void testConnection() {
        try {
            if (s3Client == null) {
                throw new S3ServiceException("S3 client not initialized");
            }
            
            ListBucketsResponse response = s3Client.listBuckets();
            logger.debug("Connection test successful. Found {} buckets", response.buckets().size());
            
        } catch (Exception e) {
            logger.error("S3 connection test failed", e);
            throw new S3ServiceException("S3 connection test failed: " + e.getMessage(), e);
        }
    }

    /**
     * List objects in the configured bucket with optional prefix.
     */
    public List<S3Object> listObjects(String prefix) {
        try {
            if (s3Client == null || currentConfig == null) {
                throw new S3ServiceException("S3 client not initialized");
            }

            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(currentConfig.getBucket())
                .delimiter("/");

            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
            
            List<S3Object> objects = new ArrayList<>();
            
            // Add directories (common prefixes)
            if (response.commonPrefixes() != null) {
                objects.addAll(response.commonPrefixes().stream()
                    .map(cp -> new S3Object(
                        cp.prefix(),
                        null,
                        0,
                        null,
                        null,
                        true
                    ))
                    .collect(Collectors.toList()));
            }
            
            // Add files
            if (response.contents() != null) {
                objects.addAll(response.contents().stream()
                    .filter(obj -> !obj.key().endsWith("/"))
                    .map(obj -> new S3Object(
                        obj.key(),
                        obj.eTag(),
                        obj.size(),
                        obj.lastModified(),
                        obj.storageClassAsString(),
                        false
                    ))
                    .collect(Collectors.toList()));
            }

            logger.debug("Listed {} objects with prefix: {}", objects.size(), prefix);
            return objects;
            
        } catch (Exception e) {
            logger.error("Failed to list objects with prefix: {}", prefix, e);
            throw new S3ServiceException("Failed to list objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get object metadata.
     */
    public S3Object getObjectMetadata(String key) {
        try {
            if (s3Client == null || currentConfig == null) {
                throw new S3ServiceException("S3 client not initialized");
            }

            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(currentConfig.getBucket())
                .key(key)
                .build();

            HeadObjectResponse response = s3Client.headObject(request);
            
            return new S3Object(
                key,
                response.eTag(),
                response.contentLength(),
                response.lastModified(),
                response.storageClassAsString(),
                false
            );
            
        } catch (Exception e) {
            logger.error("Failed to get metadata for object: {}", key, e);
            throw new S3ServiceException("Failed to get object metadata: " + e.getMessage(), e);
        }
    }

    /**
     * Download object content.
     */
    public ResponseBytes<GetObjectResponse> downloadObject(String key) {
        try {
            if (s3Client == null || currentConfig == null) {
                throw new S3ServiceException("S3 client not initialized");
            }

            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(currentConfig.getBucket())
                .key(key)
                .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
            
            logger.debug("Downloaded object: {} ({} bytes)", key, response.asByteArray().length);
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to download object: {}", key, e);
            throw new S3ServiceException("Failed to download object: " + e.getMessage(), e);
        }
    }

    /**
     * Upload object to S3.
     */
    public void uploadObject(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            if (s3Client == null || currentConfig == null) {
                throw new S3ServiceException("S3 client not initialized");
            }

            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(currentConfig.getBucket())
                .key(key);

            if (contentType != null && !contentType.isEmpty()) {
                requestBuilder.contentType(contentType);
            }

            PutObjectRequest request = requestBuilder.build();
            RequestBody requestBody = RequestBody.fromInputStream(inputStream, contentLength);

            s3Client.putObject(request, requestBody);
            
            logger.info("Successfully uploaded object: {}", key);
            
        } catch (Exception e) {
            logger.error("Failed to upload object: {}", key, e);
            throw new S3ServiceException("Failed to upload object: " + e.getMessage(), e);
        }
    }

    /**
     * Delete object from S3.
     */
    public void deleteObject(String key) {
        try {
            if (s3Client == null || currentConfig == null) {
                throw new S3ServiceException("S3 client not initialized");
            }

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(currentConfig.getBucket())
                .key(key)
                .build();

            s3Client.deleteObject(request);
            
            logger.info("Successfully deleted object: {}", key);
            
        } catch (Exception e) {
            logger.error("Failed to delete object: {}", key, e);
            throw new S3ServiceException("Failed to delete object: " + e.getMessage(), e);
        }
    }

    /**
     * List all buckets.
     */
    public List<String> listBuckets() {
        try {
            if (s3Client == null) {
                throw new S3ServiceException("S3 client not initialized");
            }

            ListBucketsResponse response = s3Client.listBuckets();
            
            return response.buckets().stream()
                .map(Bucket::name)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Failed to list buckets", e);
            throw new S3ServiceException("Failed to list buckets: " + e.getMessage(), e);
        }
    }

    /**
     * Get current S3 configuration.
     */
    public S3Configuration getCurrentConfig() {
        return currentConfig;
    }

    /**
     * Check if S3 client is initialized.
     */
    public boolean isInitialized() {
        return s3Client != null && currentConfig != null;
    }
}