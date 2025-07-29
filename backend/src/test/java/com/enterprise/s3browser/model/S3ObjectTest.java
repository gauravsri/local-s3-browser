package com.enterprise.s3browser.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class S3ObjectTest {

    @Test
    void testDefaultConstructor() {
        S3Object obj = new S3Object();
        
        assertNull(obj.getKey());
        assertNull(obj.getEtag());
        assertEquals(0L, obj.getSize());
        assertNull(obj.getLastModified());
        assertNull(obj.getStorageClass());
        assertFalse(obj.isDirectory());
    }

    @Test
    void testParameterizedConstructor() {
        Instant now = Instant.now();
        S3Object obj = new S3Object(
                "test-key",
                "etag123",
                1024L,
                now,
                "STANDARD",
                false
        );
        
        assertEquals("test-key", obj.getKey());
        assertEquals("etag123", obj.getEtag());
        assertEquals(1024L, obj.getSize());
        assertEquals(now, obj.getLastModified());
        assertEquals("STANDARD", obj.getStorageClass());
        assertFalse(obj.isDirectory());
    }

    @Test
    void testDirectoryObject() {
        S3Object obj = new S3Object(
                "folder/",
                null,
                0L,
                null,
                null,
                true
        );
        
        assertEquals("folder/", obj.getKey());
        assertNull(obj.getEtag());
        assertEquals(0L, obj.getSize());
        assertNull(obj.getLastModified());
        assertNull(obj.getStorageClass());
        assertTrue(obj.isDirectory());
    }

    @Test
    void testSettersAndGetters() {
        S3Object obj = new S3Object();
        Instant now = Instant.now();
        
        obj.setKey("test-key");
        obj.setEtag("etag123");
        obj.setSize(1024L);
        obj.setLastModified(now);
        obj.setStorageClass("STANDARD");
        obj.setDirectory(true);
        
        assertEquals("test-key", obj.getKey());
        assertEquals("etag123", obj.getEtag());
        assertEquals(1024L, obj.getSize());
        assertEquals(now, obj.getLastModified());
        assertEquals("STANDARD", obj.getStorageClass());
        assertTrue(obj.isDirectory());
    }

    @Test
    void testEqualsAndHashCode() {
        Instant now = Instant.now();
        
        S3Object obj1 = new S3Object(
                "test-key",
                "etag123",
                1024L,
                now,
                "STANDARD",
                false
        );
        
        S3Object obj2 = new S3Object(
                "test-key",
                "etag123",
                1024L,
                now,
                "STANDARD",
                false
        );
        
        assertEquals(obj1, obj2);
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testNotEquals() {
        Instant now = Instant.now();
        
        S3Object obj1 = new S3Object(
                "test-key1",
                "etag123",
                1024L,
                now,
                "STANDARD",
                false
        );
        
        S3Object obj2 = new S3Object(
                "test-key2",
                "etag123",
                1024L,
                now,
                "STANDARD",
                false
        );
        
        assertNotEquals(obj1, obj2);
    }
}