package com.orkestra.app.web;

import com.orkestra.api.model.*;
import com.orkestra.app.web.generated.WorkflowApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class WorkflowController implements WorkflowApi {

    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class);


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
        return null;
    }
}
