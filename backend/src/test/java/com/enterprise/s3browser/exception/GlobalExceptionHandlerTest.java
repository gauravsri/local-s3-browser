package com.enterprise.s3browser.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleS3ServiceException() {
        String errorMessage = "S3 operation failed";
        S3ServiceException exception = new S3ServiceException(errorMessage);
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            globalExceptionHandler.handleS3ServiceException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("S3 Service Error", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleS3ServiceExceptionWithNullMessage() {
        S3ServiceException exception = new S3ServiceException(null);
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            globalExceptionHandler.handleS3ServiceException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody().getMessage());
    }

    @Test
    void testHandleAuthenticationException() {
        String errorMessage = "Authentication failed";
        AuthenticationException exception = new AuthenticationException(errorMessage);
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            globalExceptionHandler.handleAuthenticationException(exception, webRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("Authentication Error", response.getBody().getError());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void testHandleValidationExceptions() {
        FieldError fieldError = new FieldError("object", "field", "Field is required");
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            globalExceptionHandler.handleValidationExceptions(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input parameters", response.getBody().getMessage());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getValidationErrors());
        assertTrue(response.getBody().getValidationErrors().containsKey("field"));
    }

    @Test
    void testHandleGenericException() {
        String errorMessage = "General error";
        RuntimeException exception = new RuntimeException(errorMessage);
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            globalExceptionHandler.handleGenericException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    void testErrorResponseConstructor() {
        GlobalExceptionHandler.ErrorResponse errorResponse = 
            new GlobalExceptionHandler.ErrorResponse(400, "Bad Request", "Test message", java.time.Instant.now());
        
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Test message", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void testErrorResponseWithValidationErrors() {
        java.util.Map<String, String> validationErrors = new java.util.HashMap<>();
        validationErrors.put("field1", "Error 1");
        
        GlobalExceptionHandler.ErrorResponse errorResponse = 
            new GlobalExceptionHandler.ErrorResponse(400, "Validation Error", "Invalid input", 
                java.time.Instant.now(), validationErrors);
        
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Validation Error", errorResponse.getError());
        assertEquals("Invalid input", errorResponse.getMessage());
        assertNotNull(errorResponse.getValidationErrors());
        assertEquals("Error 1", errorResponse.getValidationErrors().get("field1"));
    }
}