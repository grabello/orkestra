package com.orkestra.app.web;

import com.orkestra.api.model.ListWorkflowVersionsResponse;
import com.orkestra.api.model.ListWorkflowsResponse;
import com.orkestra.api.model.RegisterWorkflowResponse;
import com.orkestra.api.model.WorkflowVersion;
import com.orkestra.app.service.WorkflowRegistrationService;
import com.orkestra.app.web.generated.WorkflowApi;
import com.orkestra.app.web.util.WorkflowFileReader;
import com.orkestra.exception.FileProcessingException;
import com.orkestra.exception.UnsupportedMediaTypeException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@RestController
public class WorkflowController implements WorkflowApi {


    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class);

    private final WorkflowFileReader workflowFileReader;

    private final WorkflowRegistrationService workflowRegistrationService;

    @Override
    public ResponseEntity<WorkflowVersion> getWorkflowVersion(String name, Integer version) {
        return null;
    }

    @Override
    public ResponseEntity<ListWorkflowVersionsResponse> listWorkflowVersions(String name) {
        return null;
    }

    @Override
    public ResponseEntity<ListWorkflowsResponse> listWorkflows(String cursor, Integer limit) {
        return null;
    }

    @Override
    public ResponseEntity<RegisterWorkflowResponse> registerWorkflow(String name, MultipartFile definition) {
        OffsetDateTime createdAt = OffsetDateTime.now();
        log.info("Received workflow file name={} size={}B", name, definition.getSize());

        if (definition.isEmpty()) {
            throw new FileProcessingException("WORKFLOW_FILE_EMPTY", "No workflow file provided");
        }

        String contentType = definition.getContentType();
        if (contentType == null ||
                (!contentType.equals("text/yaml") &&
                        !contentType.equals("application/yaml") &&
                        !contentType.equals("application/x-yaml"))) {
            throw new UnsupportedMediaTypeException("Unsupported file type: " + contentType);
        }

        String yamlString = workflowFileReader.read(definition);

        workflowRegistrationService.register(name, yamlString);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new RegisterWorkflowResponse()
                        .name(name)
                        .version(1)
                        .createdAt(createdAt));
    }
}
