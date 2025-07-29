package com.enterprise.s3browser.service;

import com.enterprise.s3browser.model.S3Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ConfigurationServiceTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private S3ConfigurationService s3ConfigurationService;

    private S3Configuration testConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3ConfigurationService, "defaultEndpoint", "http://localhost:9000");
        ReflectionTestUtils.setField(s3ConfigurationService, "defaultAccessKey", "testkey");
        ReflectionTestUtils.setField(s3ConfigurationService, "defaultSecretKey", "testsecret");
        ReflectionTestUtils.setField(s3ConfigurationService, "defaultBucket", "test-bucket");
        ReflectionTestUtils.setField(s3ConfigurationService, "defaultRegion", "us-east-1");
        ReflectionTestUtils.setField(s3ConfigurationService, "defaultPathStyleAccess", true);

        testConfig = new S3Configuration(
                "http://localhost:9000",
                "testkey",
                "testsecret",
                "test-bucket",
                "us-east-1",
                true
        );
    }

    @Test
    void initializeWithDefaults_Success() {
        doNothing().when(s3Service).initializeClient(any(S3Configuration.class));

        assertDoesNotThrow(() -> s3ConfigurationService.initializeWithDefaults());

        verify(s3Service).initializeClient(any(S3Configuration.class));
    }

    @Test
    void initializeWithDefaults_ServiceThrowsException() {
        doThrow(new RuntimeException("Test exception")).when(s3Service).initializeClient(any(S3Configuration.class));

        assertDoesNotThrow(() -> s3ConfigurationService.initializeWithDefaults());

        verify(s3Service).initializeClient(any(S3Configuration.class));
    }

    @Test
    void updateConfiguration_Success() {
        doNothing().when(s3Service).initializeClient(testConfig);

        assertDoesNotThrow(() -> s3ConfigurationService.updateConfiguration(testConfig));

        verify(s3Service).initializeClient(testConfig);
    }

    @Test
    void updateConfiguration_NullConfig() {
        assertThrows(IllegalArgumentException.class, 
                () -> s3ConfigurationService.updateConfiguration(null));

        verify(s3Service, never()).initializeClient(any());
    }

    @Test
    void updateConfiguration_InvalidEndpoint() {
        testConfig.setEndpoint("");

        assertThrows(IllegalArgumentException.class, 
                () -> s3ConfigurationService.updateConfiguration(testConfig));

        verify(s3Service, never()).initializeClient(any());
    }

    @Test
    void updateConfiguration_InvalidAccessKey() {
        testConfig.setAccessKey(null);

        assertThrows(IllegalArgumentException.class, 
                () -> s3ConfigurationService.updateConfiguration(testConfig));

        verify(s3Service, never()).initializeClient(any());
    }

    @Test
    void updateConfiguration_InvalidSecretKey() {
        testConfig.setSecretKey("");

        assertThrows(IllegalArgumentException.class, 
                () -> s3ConfigurationService.updateConfiguration(testConfig));

        verify(s3Service, never()).initializeClient(any());
    }

    @Test
    void updateConfiguration_InvalidBucket() {
        testConfig.setBucket(null);

        assertThrows(IllegalArgumentException.class, 
                () -> s3ConfigurationService.updateConfiguration(testConfig));

        verify(s3Service, never()).initializeClient(any());
    }

    @Test
    void updateConfiguration_InvalidRegion() {
        testConfig.setRegion("");

        assertThrows(IllegalArgumentException.class, 
                () -> s3ConfigurationService.updateConfiguration(testConfig));

        verify(s3Service, never()).initializeClient(any());
    }

    @Test
    void updateConfiguration_InvalidEndpointFormat() {
        testConfig.setEndpoint("invalid-url");

        assertThrows(IllegalArgumentException.class, 
                () -> s3ConfigurationService.updateConfiguration(testConfig));

        verify(s3Service, never()).initializeClient(any());
    }

    @Test
    void getCurrentConfiguration_WithCurrentConfig() {
        when(s3Service.getCurrentConfig()).thenReturn(testConfig);

        S3Configuration result = s3ConfigurationService.getCurrentConfiguration();

        assertNotNull(result);
        assertEquals(testConfig.getEndpoint(), result.getEndpoint());
        assertEquals(testConfig.getAccessKey(), result.getAccessKey());
        assertEquals("***MASKED***", result.getSecretKey());
        assertEquals(testConfig.getBucket(), result.getBucket());
        assertEquals(testConfig.getRegion(), result.getRegion());
        assertEquals(testConfig.isPathStyleAccess(), result.isPathStyleAccess());
    }

    @Test
    void getCurrentConfiguration_WithoutCurrentConfig() {
        when(s3Service.getCurrentConfig()).thenReturn(null);

        S3Configuration result = s3ConfigurationService.getCurrentConfiguration();

        assertNotNull(result);
        assertEquals("http://localhost:9000", result.getEndpoint());
        assertEquals("testkey", result.getAccessKey());
        assertEquals("***MASKED***", result.getSecretKey());
        assertEquals("test-bucket", result.getBucket());
        assertEquals("us-east-1", result.getRegion());
        assertTrue(result.isPathStyleAccess());
    }

    @Test
    void getCurrentConfigurationInternal_WithCurrentConfig() {
        when(s3Service.getCurrentConfig()).thenReturn(testConfig);

        S3Configuration result = s3ConfigurationService.getCurrentConfigurationInternal();

        assertEquals(testConfig, result);
    }

    @Test
    void getCurrentConfigurationInternal_WithoutCurrentConfig() {
        when(s3Service.getCurrentConfig()).thenReturn(null);

        S3Configuration result = s3ConfigurationService.getCurrentConfigurationInternal();

        assertNotNull(result);
        assertEquals("http://localhost:9000", result.getEndpoint());
        assertEquals("testkey", result.getAccessKey());
        assertEquals("testsecret", result.getSecretKey());
        assertEquals("test-bucket", result.getBucket());
        assertEquals("us-east-1", result.getRegion());
        assertTrue(result.isPathStyleAccess());
    }

    @Test
    void testCurrentConnection_Success() {
        doNothing().when(s3Service).testConnection();

        assertDoesNotThrow(() -> s3ConfigurationService.testCurrentConnection());

        verify(s3Service).testConnection();
    }

    @Test
    void testConfiguration_Success() {
        // This test is more complex since it creates a new S3Service instance
        // For now, we'll test the happy path with mocking
        assertDoesNotThrow(() -> s3ConfigurationService.testConfiguration(testConfig));
    }

    @Test
    void isInitialized_True() {
        when(s3Service.isInitialized()).thenReturn(true);

        assertTrue(s3ConfigurationService.isInitialized());

        verify(s3Service).isInitialized();
    }

    @Test
    void isInitialized_False() {
        when(s3Service.isInitialized()).thenReturn(false);

        assertFalse(s3ConfigurationService.isInitialized());

        verify(s3Service).isInitialized();
    }
}