package com.orkestra.app.web;

import com.orkestra.api.model.ListWorkflowVersionsResponse;
import com.orkestra.api.model.ListWorkflowsResponse;
import com.orkestra.api.model.RegisterWorkflowResponse;
import com.orkestra.api.model.WorkflowVersion;
import com.orkestra.app.security.CurrentUserProvider;
import com.orkestra.app.service.WorkflowManagementService;
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

@RequiredArgsConstructor
@RestController
public class WorkflowController implements WorkflowApi {


    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class);

    private final WorkflowFileReader workflowFileReader;
    private final WorkflowManagementService workflowManagementService;

    @Override
    public ResponseEntity<WorkflowVersion> getWorkflowVersion(String xTenantId, String name, Integer version) {
        log.info("Received request to get workflow version name={} version={}", name, version);
        return ResponseEntity.ok(workflowManagementService.getWorkflowVersion(xTenantId, name, version));
    }

    @Override
    public ResponseEntity<ListWorkflowVersionsResponse> listWorkflowVersions(String xTenantId, String name) {
        log.info("Received request to list workflow versions name={}", name);
        return ResponseEntity.ok(workflowManagementService.listWorkflowVersions(xTenantId, name));
    }

    @Override
    public ResponseEntity<ListWorkflowsResponse> listWorkflows(String xTenantId, String cursor, Integer limit) {
        log.info("Received request to list workflows cursor={} limit={}", cursor, limit);
        return ResponseEntity.ok(workflowManagementService.listWorkflows(xTenantId, cursor, limit));
    }

    @Override
    public ResponseEntity<RegisterWorkflowResponse> registerWorkflow(String xTenantId, String name, MultipartFile definition) {
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

        RegisterWorkflowResponse response = workflowManagementService.register(xTenantId, name, yamlString);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
