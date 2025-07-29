package com.enterprise.s3browser.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void testParameterizedConstructor() {
        AuthResponse response = new AuthResponse("test-token", "testuser");
        
        assertEquals("test-token", response.getAccessToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("Bearer", response.getType());
    }

    @Test
    void testDefaultConstructor() {
        AuthResponse response = new AuthResponse();
        
        assertNull(response.getAccessToken());
        assertNull(response.getUsername());
        assertEquals("Bearer", response.getType());
    }

    @Test
    void testSettersAndGetters() {
        AuthResponse response = new AuthResponse();
        
        response.setAccessToken("test-token");
        response.setUsername("testuser");
        response.setType("Custom");
        
        assertEquals("test-token", response.getAccessToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("Custom", response.getType());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthResponse response1 = new AuthResponse("test-token", "testuser");
        AuthResponse response2 = new AuthResponse("test-token", "testuser");
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testNotEquals() {
        AuthResponse response1 = new AuthResponse("test-token1", "testuser");
        AuthResponse response2 = new AuthResponse("test-token2", "testuser");
        
        assertNotEquals(response1, response2);
    }

    @Test
    void testToString() {
        AuthResponse response = new AuthResponse("test-token", "testuser");
        
        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test-token"));
    }
}