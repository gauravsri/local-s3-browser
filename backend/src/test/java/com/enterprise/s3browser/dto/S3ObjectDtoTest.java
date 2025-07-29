package com.enterprise.s3browser.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class S3ObjectDtoTest {

    @Test
    void testDefaultConstructor() {
        S3ObjectDto dto = new S3ObjectDto();
        
        assertNull(dto.getKey());
        assertNull(dto.getEtag());
        assertEquals(0L, dto.getSize());
        assertNull(dto.getLastModified());
        assertNull(dto.getStorageClass());
        assertFalse(dto.isDirectory());
    }

    @Test
    void testSettersAndGetters() {
        S3ObjectDto dto = new S3ObjectDto();
        Instant now = Instant.now();
        
        dto.setKey("test-key");
        dto.setEtag("etag123");
        dto.setSize(1024L);
        dto.setLastModified(now);
        dto.setStorageClass("STANDARD");
        dto.setDirectory(true);
        
        assertEquals("test-key", dto.getKey());
        assertEquals("etag123", dto.getEtag());
        assertEquals(1024L, dto.getSize());
        assertEquals(now, dto.getLastModified());
        assertEquals("STANDARD", dto.getStorageClass());
        assertTrue(dto.isDirectory());
    }

    @Test
    void testDirectoryObject() {
        S3ObjectDto dto = new S3ObjectDto();
        dto.setKey("folder/");
        dto.setDirectory(true);
        dto.setSize(0L);
        
        assertEquals("folder/", dto.getKey());
        assertTrue(dto.isDirectory());
        assertEquals(0L, dto.getSize());
    }

    @Test
    void testFileObject() {
        S3ObjectDto dto = new S3ObjectDto();
        dto.setKey("file.txt");
        dto.setDirectory(false);
        dto.setSize(1024L);
        dto.setEtag("etag123");
        
        assertEquals("file.txt", dto.getKey());
        assertFalse(dto.isDirectory());
        assertEquals(1024L, dto.getSize());
        assertEquals("etag123", dto.getEtag());
    }

    @Test
    void testEqualsAndHashCode() {
        Instant now = Instant.now();
        
        S3ObjectDto dto1 = new S3ObjectDto();
        dto1.setKey("test-key");
        dto1.setEtag("etag123");
        dto1.setSize(1024L);
        dto1.setLastModified(now);
        dto1.setStorageClass("STANDARD");
        dto1.setDirectory(false);
        
        S3ObjectDto dto2 = new S3ObjectDto();
        dto2.setKey("test-key");
        dto2.setEtag("etag123");
        dto2.setSize(1024L);
        dto2.setLastModified(now);
        dto2.setStorageClass("STANDARD");
        dto2.setDirectory(false);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testNotEquals() {
        S3ObjectDto dto1 = new S3ObjectDto();
        dto1.setKey("test-key1");
        
        S3ObjectDto dto2 = new S3ObjectDto();
        dto2.setKey("test-key2");
        
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToString() {
        S3ObjectDto dto = new S3ObjectDto();
        dto.setKey("test-key");
        dto.setSize(1024L);
        dto.setStorageClass("STANDARD");
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test-key"));
    }
}