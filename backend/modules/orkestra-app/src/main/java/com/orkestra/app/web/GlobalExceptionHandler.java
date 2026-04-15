package com.orkestra.app.web;

import com.orkestra.api.model.ApiError;
import com.orkestra.app.exception.AdminForbiddenException;
import com.orkestra.app.exception.MissingTenantIdException;
import com.orkestra.app.exception.TenantAccessDeniedException;
import com.orkestra.app.exception.TenantNotFoundException;
import com.orkestra.exception.FileProcessingException;
import com.orkestra.exception.UnsupportedMediaTypeException;
import com.orkestra.exception.WorkflowNotFoundException;
import com.orkestra.exception.WorkflowValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WorkflowValidationException.class)
    public ResponseEntity<ApiError> handleWorkflowValidation(
            WorkflowValidationException ex) {
        log.error("Workflow validation failed", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code(ex.getCode())
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ApiError> handleFileProcessingException(
            FileProcessingException ex) {
        log.error("File processing failed", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code(ex.getCode())
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(UnsupportedMediaTypeException.class)
    public ResponseEntity<ApiError> handleUnsupportedMediaType(
            UnsupportedMediaTypeException ex) {
        log.error("Unsupported media type", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code("UNSUPPORTED_MEDIA_TYPE")
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.error("Missing request parameter", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code("MISSING_PARAMETER")
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestPartException(
    MissingServletRequestPartException ex) {
        log.error("Missing request part", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code("MISSING_PARAMETER")
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {
        log.error("Workflow file too large", ex);

        List<String> messages = new ArrayList<>();
        messages.add("Workflow file exceeds maximum allowed size");
        ApiError error = new ApiError()
                .code("WORKFLOW_FILE_TOO_LARGE")
                .messages(messages);

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(error);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> handleThrowable(Throwable ex) {
        log.error("Unexpected error occurred", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code("INTERNAL_ERROR")
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    @ExceptionHandler(WorkflowNotFoundException.class)
    public ResponseEntity<ApiError> handleWorkflowNotFoundException(WorkflowNotFoundException ex) {
        log.error("Unexpected error occurred", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code(ex.getCode())
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);

    }

    @ExceptionHandler(MissingTenantIdException.class)
    public ResponseEntity<ApiError> handleMissingTenantIdException(MissingTenantIdException ex) {
        log.error("Missing tenantId", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code(ex.getCode())
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(AdminForbiddenException.class)
    public ResponseEntity<ApiError> handleAdminForbiddenException(AdminForbiddenException ex) {
        log.error("Admin forbidden", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code(ex.getCode())
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<ApiError> handleTenantAccessDeniedException(TenantAccessDeniedException ex) {
        log.error("Tenant access denied", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code(ex.getCode())
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiError> handleTenantNotFoundException(TenantNotFoundException ex) {
        log.error("Tenant not found", ex);

        List<String> message = ex.getMessage() == null ? List.of("Unexpected error occurred") : Arrays.stream(ex.getMessage().split("\n")).toList();

        ApiError error = new ApiError()
                .code(ex.getCode())
                .messages(message);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);

    }
}
