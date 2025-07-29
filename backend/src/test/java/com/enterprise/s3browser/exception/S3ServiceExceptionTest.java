package com.enterprise.s3browser.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class S3ServiceExceptionTest {

    @Test
    void testMessageConstructor() {
        String message = "Test error message";
        S3ServiceException exception = new S3ServiceException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        String message = "Test error message";
        RuntimeException cause = new RuntimeException("Cause message");
        S3ServiceException exception = new S3ServiceException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testNullMessage() {
        S3ServiceException exception = new S3ServiceException(null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testEmptyMessage() {
        S3ServiceException exception = new S3ServiceException("");
        
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionInheritance() {
        S3ServiceException exception = new S3ServiceException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testStackTrace() {
        S3ServiceException exception = new S3ServiceException("Test message");
        
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    void testSerialization() {
        String message = "Test serialization";
        S3ServiceException exception = new S3ServiceException(message);
        
        // Test that basic properties are preserved
        assertEquals(message, exception.getMessage());
        assertNotNull(exception.toString());
        assertTrue(exception.toString().contains("S3ServiceException"));
        assertTrue(exception.toString().contains(message));
    }
}