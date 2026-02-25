package com.orkestra.app.web.util;

import com.orkestra.exception.FileProcessingException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class WorkflowFileReader {

    public String read(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new FileProcessingException(
                    "WORKFLOW_FILE_READ_ERROR",
                    "Unable to read workflow file"
            );
        }

    }
}
