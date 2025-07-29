package com.enterprise.s3browser.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestTest {

    @Test
    void testDefaultConstructor() {
        AuthRequest request = new AuthRequest();
        
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        AuthRequest request = new AuthRequest();
        
        request.setUsername("testuser");
        request.setPassword("testpass");
        
        assertEquals("testuser", request.getUsername());
        assertEquals("testpass", request.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthRequest request1 = new AuthRequest();
        request1.setUsername("testuser");
        request1.setPassword("testpass");
        
        AuthRequest request2 = new AuthRequest();
        request2.setUsername("testuser");
        request2.setPassword("testpass");
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testNotEquals() {
        AuthRequest request1 = new AuthRequest();
        request1.setUsername("testuser1");
        request1.setPassword("testpass");
        
        AuthRequest request2 = new AuthRequest();
        request2.setUsername("testuser2");
        request2.setPassword("testpass");
        
        assertNotEquals(request1, request2);
    }

    @Test
    void testToString() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");
        
        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        // Password should not be in toString for security
    }
}