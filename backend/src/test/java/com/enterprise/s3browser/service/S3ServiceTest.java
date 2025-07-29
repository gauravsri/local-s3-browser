package com.enterprise.s3browser.service;

import com.enterprise.s3browser.exception.S3ServiceException;
import com.enterprise.s3browser.model.S3Configuration;
import com.enterprise.s3browser.model.S3Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3ClientBuilder s3ClientBuilder;

    @InjectMocks
    private S3Service s3Service;

    private S3Configuration testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new S3Configuration();
        testConfig.setEndpoint("http://localhost:9000");
        testConfig.setBucket("test-bucket");
        testConfig.setAccessKey("testkey");
        testConfig.setSecretKey("testsecret");
        testConfig.setRegion("us-east-1");
        testConfig.setPathStyleAccess(true);
    }

    @Test
    void initializeClient_Success() {
        try (MockedStatic<S3Client> mockedS3Client = mockStatic(S3Client.class)) {
            mockedS3Client.when(S3Client::builder).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.credentialsProvider(any())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.region(any())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.httpClient(any())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.endpointOverride(any())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.forcePathStyle(anyBoolean())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.build()).thenReturn(s3Client);
            
            when(s3Client.listBuckets()).thenReturn(ListBucketsResponse.builder().build());

            s3Service.initializeClient(testConfig);

            assertEquals(testConfig, s3Service.getCurrentConfig());
            assertTrue(s3Service.isInitialized());
            verify(s3Client).listBuckets();
        }
    }

    @Test
    void initializeClient_ThrowsException() {
        try (MockedStatic<S3Client> mockedS3Client = mockStatic(S3Client.class)) {
            mockedS3Client.when(S3Client::builder).thenThrow(new RuntimeException("Test exception"));

            assertThrows(S3ServiceException.class, () -> s3Service.initializeClient(testConfig));
            assertFalse(s3Service.isInitialized());
        }
    }

    @Test
    void testConnection_Success() {
        setupInitializedService();

        assertDoesNotThrow(() -> s3Service.testConnection());
        verify(s3Client, atLeast(1)).listBuckets();
    }

    @Test
    void testConnection_ClientNotInitialized() {
        assertThrows(S3ServiceException.class, () -> s3Service.testConnection());
    }

    @Test
    void listObjects_Success() {
        setupInitializedService();

        software.amazon.awssdk.services.s3.model.S3Object obj1 = 
                software.amazon.awssdk.services.s3.model.S3Object.builder()
                .key("file1.txt")
                .size(100L)
                .lastModified(Instant.now())
                .eTag("etag1")
                .storageClass("STANDARD")
                .build();

        CommonPrefix prefix1 = CommonPrefix.builder().prefix("folder1/").build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(Arrays.asList(obj1))
                .commonPrefixes(Arrays.asList(prefix1))
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        List<S3Object> result = s3Service.listObjects("prefix");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(o -> o.getKey().equals("folder1/") && o.isDirectory()));
        assertTrue(result.stream().anyMatch(o -> o.getKey().equals("file1.txt") && !o.isDirectory()));
    }

    @Test
    void listObjects_ClientNotInitialized() {
        assertThrows(S3ServiceException.class, () -> s3Service.listObjects("prefix"));
    }

    @Test
    void getObjectMetadata_Success() {
        setupInitializedService();

        HeadObjectResponse response = HeadObjectResponse.builder()
                .contentLength(100L)
                .lastModified(Instant.now())
                .eTag("etag1")
                .storageClass("STANDARD")
                .build();

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(response);

        S3Object result = s3Service.getObjectMetadata("test-key");

        assertEquals("test-key", result.getKey());
        assertEquals(100L, result.getSize());
        assertEquals("etag1", result.getEtag());
        assertFalse(result.isDirectory());
    }

    @Test
    void getObjectMetadata_ClientNotInitialized() {
        assertThrows(S3ServiceException.class, () -> s3Service.getObjectMetadata("test-key"));
    }

    @Test
    void downloadObject_Success() {
        setupInitializedService();

        byte[] content = "test content".getBytes();
        GetObjectResponse getObjectResponse = GetObjectResponse.builder().build();
        ResponseBytes<GetObjectResponse> response = ResponseBytes.fromByteArray(getObjectResponse, content);

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(response);

        ResponseBytes<GetObjectResponse> result = s3Service.downloadObject("test-key");

        assertArrayEquals(content, result.asByteArray());
    }

    @Test
    void downloadObject_ClientNotInitialized() {
        assertThrows(S3ServiceException.class, () -> s3Service.downloadObject("test-key"));
    }

    @Test
    void uploadObject_Success() {
        setupInitializedService();

        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        assertDoesNotThrow(() -> s3Service.uploadObject("test-key", inputStream, 12L, "text/plain"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadObject_ClientNotInitialized() {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        assertThrows(S3ServiceException.class, () -> s3Service.uploadObject("test-key", inputStream, 12L, "text/plain"));
    }

    @Test
    void deleteObject_Success() {
        setupInitializedService();

        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        assertDoesNotThrow(() -> s3Service.deleteObject("test-key"));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteObject_ClientNotInitialized() {
        assertThrows(S3ServiceException.class, () -> s3Service.deleteObject("test-key"));
    }

    @Test
    void listBuckets_Success() {
        setupInitializedService();

        Bucket bucket1 = Bucket.builder().name("bucket1").build();
        Bucket bucket2 = Bucket.builder().name("bucket2").build();
        ListBucketsResponse response = ListBucketsResponse.builder()
                .buckets(Arrays.asList(bucket1, bucket2))
                .build();

        when(s3Client.listBuckets()).thenReturn(response);

        List<String> result = s3Service.listBuckets();

        assertEquals(2, result.size());
        assertTrue(result.contains("bucket1"));
        assertTrue(result.contains("bucket2"));
    }

    @Test
    void listBuckets_ClientNotInitialized() {
        assertThrows(S3ServiceException.class, () -> s3Service.listBuckets());
    }

    @Test
    void createFolder_Success() {
        setupInitializedService();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        assertDoesNotThrow(() -> s3Service.createFolder("test-folder"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void createFolder_WithTrailingSlash() {
        setupInitializedService();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        assertDoesNotThrow(() -> s3Service.createFolder("test-folder/"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void createFolder_ClientNotInitialized() {
        assertThrows(S3ServiceException.class, () -> s3Service.createFolder("test-folder"));
    }

    @Test
    void getCurrentConfig_ReturnsCorrectConfig() {
        setupInitializedService();
        assertEquals(testConfig, s3Service.getCurrentConfig());
    }

    @Test
    void isInitialized_ReturnsFalseWhenNotInitialized() {
        assertFalse(s3Service.isInitialized());
    }

    private void setupInitializedService() {
        try (MockedStatic<S3Client> mockedS3Client = mockStatic(S3Client.class)) {
            mockedS3Client.when(S3Client::builder).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.credentialsProvider(any())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.region(any())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.httpClient(any())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.endpointOverride(any())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.forcePathStyle(anyBoolean())).thenReturn(s3ClientBuilder);
            when(s3ClientBuilder.build()).thenReturn(s3Client);
            
            when(s3Client.listBuckets()).thenReturn(ListBucketsResponse.builder().build());

            s3Service.initializeClient(testConfig);
        }
    }
}