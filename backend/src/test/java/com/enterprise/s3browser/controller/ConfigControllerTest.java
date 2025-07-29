package com.enterprise.s3browser.controller;

import com.enterprise.s3browser.dto.S3ConfigurationDto;
import com.enterprise.s3browser.model.S3Configuration;
import com.enterprise.s3browser.service.S3ConfigurationService;
import com.enterprise.s3browser.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(controllers = ConfigController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3ConfigurationService configurationService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    private S3Configuration testConfig;
    private S3ConfigurationDto testConfigDto;

    @BeforeEach
    void setUp() {
        testConfig = new S3Configuration(
                "http://localhost:9000",
                "testkey",
                "***MASKED***",
                "test-bucket",
                "us-east-1",
                true
        );

        testConfigDto = new S3ConfigurationDto();
        testConfigDto.setEndpoint("http://localhost:9000");
        testConfigDto.setAccessKey("testkey");
        testConfigDto.setSecretKey("testsecret");
        testConfigDto.setBucket("test-bucket");
        testConfigDto.setRegion("us-east-1");
        testConfigDto.setPathStyleAccess(true);
    }

    @Test
    void getCurrentConfiguration_Success() throws Exception {
        when(configurationService.getCurrentConfiguration()).thenReturn(testConfig);

        mockMvc.perform(get("/api/config")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoint").value("http://localhost:9000"))
                .andExpect(jsonPath("$.accessKey").value("testkey"))
                .andExpect(jsonPath("$.secretKey").value("test****"))
                .andExpect(jsonPath("$.bucket").value("test-bucket"))
                .andExpect(jsonPath("$.region").value("us-east-1"))
                .andExpect(jsonPath("$.pathStyleAccess").value(true));

        verify(configurationService).getCurrentConfiguration();
    }

    @Test
    void updateConfiguration_Success() throws Exception {
        doNothing().when(configurationService).updateConfiguration(any(S3Configuration.class));
        doNothing().when(s3Service).initializeClient(any(S3Configuration.class));

        mockMvc.perform(put("/api/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfigDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Configuration updated successfully"));

        verify(configurationService).updateConfiguration(any(S3Configuration.class));
        verify(s3Service).initializeClient(any(S3Configuration.class));
    }

    @Test
    void updateConfiguration_InvalidInput() throws Exception {
        S3ConfigurationDto invalidDto = new S3ConfigurationDto();
        // Missing required fields

        mockMvc.perform(put("/api/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(configurationService, never()).updateConfiguration(any());
        verify(s3Service, never()).initializeClient(any());
    }

    @Test
    void testConfiguration_Success() throws Exception {
        doNothing().when(configurationService).testConfiguration(any(S3Configuration.class));

        mockMvc.perform(post("/api/config/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfigDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Configuration is valid and connection successful"));

        verify(configurationService).testConfiguration(any(S3Configuration.class));
    }

    @Test
    void testConfiguration_Failure() throws Exception {
        doThrow(new RuntimeException("Connection failed"))
                .when(configurationService).testConfiguration(any(S3Configuration.class));

        mockMvc.perform(post("/api/config/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfigDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Configuration test failed")));

        verify(configurationService).testConfiguration(any(S3Configuration.class));
    }

    @Test
    void testConfiguration_InvalidInput() throws Exception {
        S3ConfigurationDto invalidDto = new S3ConfigurationDto();
        // Missing required fields

        mockMvc.perform(post("/api/config/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(configurationService, never()).testConfiguration(any());
    }
}