package com.enterprise.s3browser.controller;

import com.enterprise.s3browser.config.SecurityConfig;
import com.enterprise.s3browser.dto.S3ObjectDto;
import com.enterprise.s3browser.model.S3Object;
import com.enterprise.s3browser.security.CustomUserDetailsService;
import com.enterprise.s3browser.security.JwtTokenProvider;
import com.enterprise.s3browser.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(controllers = S3Controller.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
class S3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    private S3Object testObject;
    private List<S3Object> testObjects;

    @BeforeEach
    void setUp() {
        testObject = new S3Object(
                "test-file.txt",
                "etag123",
                1024L,
                Instant.now(),
                "STANDARD",
                false
        );

        S3Object testFolder = new S3Object(
                "test-folder/",
                null,
                0L,
                null,
                null,
                true
        );

        testObjects = Arrays.asList(testObject, testFolder);
    }

    @Test
    void listObjects_Success() throws Exception {
        when(s3Service.listObjects("test-prefix")).thenReturn(testObjects);

        mockMvc.perform(get("/api/s3/objects")
                        .param("prefix", "test-prefix")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].key").value("test-file.txt"))
                .andExpect(jsonPath("$[0].directory").value(false))
                .andExpect(jsonPath("$[1].key").value("test-folder/"))
                .andExpect(jsonPath("$[1].directory").value(true));

        verify(s3Service).listObjects("test-prefix");
    }

    @Test
    void listObjects_WithoutPrefix() throws Exception {
        when(s3Service.listObjects(null)).thenReturn(testObjects);

        mockMvc.perform(get("/api/s3/objects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(s3Service).listObjects(null);
    }


    @Test
    void getObjectMetadata_Success() throws Exception {
        when(s3Service.getObjectMetadata("test-file.txt")).thenReturn(testObject);

        mockMvc.perform(get("/api/s3/objects/test-file.txt/metadata")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("test-file.txt"))
                .andExpect(jsonPath("$.etag").value("etag123"))
                .andExpect(jsonPath("$.size").value(1024))
                .andExpect(jsonPath("$.directory").value(false));

        verify(s3Service).getObjectMetadata("test-file.txt");
    }

    @Test
    void downloadObject_Success() throws Exception {
        byte[] content = "test file content".getBytes();
        GetObjectResponse getObjectResponse = GetObjectResponse.builder()
                .contentType("text/plain")
                .build();
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(getObjectResponse, content);

        when(s3Service.downloadObject("test-file.txt")).thenReturn(responseBytes);

        mockMvc.perform(get("/api/s3/objects/download")
                        .param("key", "test-file.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(content().bytes(content));

        verify(s3Service).downloadObject("test-file.txt");
    }

    @Test
    void uploadObject_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/api/s3/objects")
                        .file(file)
                        .param("key", "test-key")
)
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Object uploaded successfully")));

        verify(s3Service).uploadObject(eq("test-key"), any(), eq(12L), eq("text/plain"));
    }

    @Test
    void deleteObject_Success() throws Exception {
        mockMvc.perform(delete("/api/s3/objects")
                        .param("key", "test-key")
)
                .andExpect(status().isNoContent());

        verify(s3Service).deleteObject("test-key");
    }

    @Test
    void listBuckets_Success() throws Exception {
        List<String> buckets = Arrays.asList("bucket1", "bucket2");
        when(s3Service.listBuckets()).thenReturn(buckets);

        mockMvc.perform(get("/api/s3/buckets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("bucket1"))
                .andExpect(jsonPath("$[1]").value("bucket2"));

        verify(s3Service).listBuckets();
    }

    @Test
    void createFolder_Success() throws Exception {
        mockMvc.perform(post("/api/s3/folders")
                        .param("folderPath", "test-folder")
)
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Folder created successfully")));

        verify(s3Service).createFolder("test-folder");
    }

    @Test
    void testConnection_Success() throws Exception {
        mockMvc.perform(get("/api/s3/test-connection"))
                .andExpect(status().isOk())
                .andExpect(content().string("Connection successful"));

        verify(s3Service).testConnection();
    }
}