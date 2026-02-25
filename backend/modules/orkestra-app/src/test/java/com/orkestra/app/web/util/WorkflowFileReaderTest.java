package com.orkestra.app.web.util;

import com.orkestra.exception.FileProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class WorkflowFileReaderTest {

    /**
     * Tests for the WorkflowFileReader class.
     * <p>
     * The WorkflowFileReader class is responsible for reading the content
     * of a workflow file provided as a MultipartFile object and returning
     * it as a String. It handles exceptions that occur during file reading
     * and throws a custom FileProcessingException when an error occurs.
     */

    @Test
    void testRead_ValidFile() throws IOException {
        // Arrange
        WorkflowFileReader workflowFileReader = new WorkflowFileReader();
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        String fileContent = "Test workflow content";
        byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        when(mockFile.getBytes()).thenReturn(fileBytes);

        // Act
        String result = workflowFileReader.read(mockFile);

        // Assert
        assertNotNull(result);
        assertEquals(fileContent, result);
    }

    @Test
    void testRead_IOExceptionThrown() throws IOException {
        // Arrange
        WorkflowFileReader workflowFileReader = new WorkflowFileReader();
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        when(mockFile.getBytes()).thenThrow(new IOException("File read error"));

        // Act & Assert
        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> workflowFileReader.read(mockFile)
        );

        assertEquals("WORKFLOW_FILE_READ_ERROR", exception.getCode());
        assertEquals("Unable to read workflow file", exception.getMessage());
    }

    @Test
    void testRead_EmptyFile() throws IOException {
        // Arrange
        WorkflowFileReader workflowFileReader = new WorkflowFileReader();
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        byte[] fileBytes = new byte[0];
        when(mockFile.getBytes()).thenReturn(fileBytes);

        // Act
        String result = workflowFileReader.read(mockFile);

        // Assert
        assertNotNull(result);
        assertEquals("", result);
    }
}
