package com.orkestra.app.web;

import com.orkestra.api.model.ApiError;
import com.orkestra.exception.FileProcessingException;
import com.orkestra.exception.UnsupportedMediaTypeException;
import com.orkestra.exception.WorkflowValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void testHandleThrowable() {
        Throwable throwable = new RuntimeException("Unexpected error occurred");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleThrowable(throwable);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("Unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void testHandleWorkflowValidation() {
        WorkflowValidationException ex = new WorkflowValidationException("VALIDATION_ERROR", "Validation failed");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleWorkflowValidation(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void testHandleFileProcessingException() {
        FileProcessingException ex = new FileProcessingException("FILE_PROCESSING_ERROR", "File processing failed");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleFileProcessingException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FILE_PROCESSING_ERROR", response.getBody().getCode());
        assertEquals("File processing failed", response.getBody().getMessage());
    }

    @Test
    void testHandleUnsupportedMediaType() {
        UnsupportedMediaTypeException ex = new UnsupportedMediaTypeException("Media type not supported");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleUnsupportedMediaType(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNSUPPORTED_MEDIA_TYPE", response.getBody().getCode());
        assertEquals("Media type not supported", response.getBody().getMessage());
    }

    @Test
    void testHandleMissingServletRequestParameter() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("paramName", "String");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleMissingServletRequestParameterException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MISSING_PARAMETER", response.getBody().getCode());
        assertEquals("Required request parameter 'paramName' for method parameter type String is not present",
                response.getBody().getMessage());
    }

    @Test
    void testHandleMissingServletRequestPart() {
        MissingServletRequestPartException ex = new MissingServletRequestPartException("file");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleMissingServletRequestPartException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MISSING_PARAMETER", response.getBody().getCode());
        assertEquals("Required part 'file' is not present.", response.getBody().getMessage());
    }

    @Test
    void testHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024);

        ResponseEntity<ApiError> response = globalExceptionHandler.handleMaxUploadSizeExceededException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("WORKFLOW_FILE_TOO_LARGE", response.getBody().getCode());
        assertEquals("Workflow file exceeds maximum allowed size", response.getBody().getMessage());
    }
}
