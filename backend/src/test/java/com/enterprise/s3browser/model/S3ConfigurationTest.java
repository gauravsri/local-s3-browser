package com.enterprise.s3browser.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class S3ConfigurationTest {

    @Test
    void testDefaultConstructor() {
        S3Configuration config = new S3Configuration();
        
        assertNull(config.getEndpoint());
        assertNull(config.getAccessKey());
        assertNull(config.getSecretKey());
        assertNull(config.getBucket());
        assertNull(config.getRegion());
        assertFalse(config.isPathStyleAccess());
    }

    @Test
    void testParameterizedConstructor() {
        S3Configuration config = new S3Configuration(
                "http://localhost:9000",
                "testkey",
                "testsecret",
                "test-bucket",
                "us-east-1",
                true
        );
        
        assertEquals("http://localhost:9000", config.getEndpoint());
        assertEquals("testkey", config.getAccessKey());
        assertEquals("testsecret", config.getSecretKey());
        assertEquals("test-bucket", config.getBucket());
        assertEquals("us-east-1", config.getRegion());
        assertTrue(config.isPathStyleAccess());
    }

    @Test
    void testSettersAndGetters() {
        S3Configuration config = new S3Configuration();
        
        config.setEndpoint("http://localhost:9000");
        config.setAccessKey("testkey");
        config.setSecretKey("testsecret");
        config.setBucket("test-bucket");
        config.setRegion("us-east-1");
        config.setPathStyleAccess(true);
        
        assertEquals("http://localhost:9000", config.getEndpoint());
        assertEquals("testkey", config.getAccessKey());
        assertEquals("testsecret", config.getSecretKey());
        assertEquals("test-bucket", config.getBucket());
        assertEquals("us-east-1", config.getRegion());
        assertTrue(config.isPathStyleAccess());
    }

    @Test
    void testEqualsAndHashCode() {
        S3Configuration config1 = new S3Configuration(
                "http://localhost:9000",
                "testkey",
                "testsecret",
                "test-bucket",
                "us-east-1",
                true
        );
        
        S3Configuration config2 = new S3Configuration(
                "http://localhost:9000",
                "testkey",
                "testsecret",
                "test-bucket",
                "us-east-1",
                true
        );
        
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testNotEquals() {
        S3Configuration config1 = new S3Configuration(
                "http://localhost:9000",
                "testkey",
                "testsecret",
                "test-bucket",
                "us-east-1",
                true
        );
        
        S3Configuration config2 = new S3Configuration(
                "http://localhost:9001",
                "testkey",
                "testsecret",
                "test-bucket",
                "us-east-1",
                true
        );
        
        assertNotEquals(config1, config2);
    }
}