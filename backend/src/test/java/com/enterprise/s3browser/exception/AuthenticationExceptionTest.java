package com.enterprise.s3browser.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationExceptionTest {

    @Test
    void testMessageConstructor() {
        String message = "Authentication failed";
        AuthenticationException exception = new AuthenticationException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        String message = "Authentication failed";
        RuntimeException cause = new RuntimeException("Token expired");
        AuthenticationException exception = new AuthenticationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testNullMessage() {
        AuthenticationException exception = new AuthenticationException(null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testEmptyMessage() {
        AuthenticationException exception = new AuthenticationException("");
        
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionInheritance() {
        AuthenticationException exception = new AuthenticationException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testStackTrace() {
        AuthenticationException exception = new AuthenticationException("Test message");
        
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    void testSerialization() {
        String message = "Authentication error";
        AuthenticationException exception = new AuthenticationException(message);
        
        assertEquals(message, exception.getMessage());
        assertNotNull(exception.toString());
        assertTrue(exception.toString().contains("AuthenticationException"));
        assertTrue(exception.toString().contains(message));
    }
}