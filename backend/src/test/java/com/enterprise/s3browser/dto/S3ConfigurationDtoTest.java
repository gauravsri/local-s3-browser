package com.enterprise.s3browser.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class S3ConfigurationDtoTest {

    @Test
    void testDefaultConstructor() {
        S3ConfigurationDto dto = new S3ConfigurationDto();
        
        assertNull(dto.getEndpoint());
        assertNull(dto.getAccessKey());
        assertNull(dto.getSecretKey());
        assertNull(dto.getBucket());
        assertNull(dto.getRegion());
        assertNull(dto.getPathStyleAccess());
    }

    @Test
    void testSettersAndGetters() {
        S3ConfigurationDto dto = new S3ConfigurationDto();
        
        dto.setEndpoint("http://localhost:9000");
        dto.setAccessKey("testkey");
        dto.setSecretKey("testsecret");
        dto.setBucket("test-bucket");
        dto.setRegion("us-east-1");
        dto.setPathStyleAccess(true);
        
        assertEquals("http://localhost:9000", dto.getEndpoint());
        assertEquals("testkey", dto.getAccessKey());
        assertEquals("testsecret", dto.getSecretKey());
        assertEquals("test-bucket", dto.getBucket());
        assertEquals("us-east-1", dto.getRegion());
        assertTrue(dto.getPathStyleAccess());
    }

    @Test
    void testEqualsAndHashCode() {
        S3ConfigurationDto dto1 = new S3ConfigurationDto();
        dto1.setEndpoint("http://localhost:9000");
        dto1.setAccessKey("testkey");
        dto1.setSecretKey("testsecret");
        dto1.setBucket("test-bucket");
        dto1.setRegion("us-east-1");
        dto1.setPathStyleAccess(true);
        
        S3ConfigurationDto dto2 = new S3ConfigurationDto();
        dto2.setEndpoint("http://localhost:9000");
        dto2.setAccessKey("testkey");
        dto2.setSecretKey("testsecret");
        dto2.setBucket("test-bucket");
        dto2.setRegion("us-east-1");
        dto2.setPathStyleAccess(true);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testNotEquals() {
        S3ConfigurationDto dto1 = new S3ConfigurationDto();
        dto1.setEndpoint("http://localhost:9000");
        dto1.setAccessKey("testkey");
        
        S3ConfigurationDto dto2 = new S3ConfigurationDto();
        dto2.setEndpoint("http://localhost:9001");
        dto2.setAccessKey("testkey");
        
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToString() {
        S3ConfigurationDto dto = new S3ConfigurationDto();
        dto.setEndpoint("http://localhost:9000");
        dto.setAccessKey("testkey");
        dto.setBucket("test-bucket");
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("http://localhost:9000"));
        assertTrue(toString.contains("testkey"));
        assertTrue(toString.contains("test-bucket"));
    }
}