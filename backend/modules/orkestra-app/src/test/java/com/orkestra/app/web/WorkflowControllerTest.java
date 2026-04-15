package com.orkestra.app.web;

import com.orkestra.api.model.ListWorkflowVersionsResponse;
import com.orkestra.api.model.ListWorkflowsResponse;
import com.orkestra.api.model.RegisterWorkflowResponse;
import com.orkestra.api.model.WorkflowListItem;
import com.orkestra.api.model.WorkflowVersion;
import com.orkestra.api.model.WorkflowVersionMetadata;
import com.orkestra.app.security.TenantFilter;
import com.orkestra.app.service.WorkflowManagementService;
import com.orkestra.app.web.util.WorkflowFileReader;
import com.orkestra.exception.FileProcessingException;
import com.orkestra.exception.UnsupportedMediaTypeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = WorkflowController.class)
class WorkflowControllerTest {

    @Autowired
    private WorkflowController controller;

    @MockBean
    private WorkflowFileReader workflowFileReader;

    @MockBean
    private WorkflowManagementService workflowManagementService;

    @MockBean
    private TenantFilter tenantFilter;

    @Test
    @DisplayName("registerWorkflow: happy path with application/yaml file")
    void registerWorkflow_happyPath() {
        String name = "invoice-processing";
        byte[] content = "name: invoice-processing".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "definition",
                "workflow.yaml",
                "application/yaml",
                content
        );

        when(workflowFileReader.read(file)).thenReturn(new String(content));
        RegisterWorkflowResponse expectedResponse = new RegisterWorkflowResponse();
        expectedResponse.setName(name);
        expectedResponse.setVersion(1);
        expectedResponse.setCreatedAt(OffsetDateTime.now());
        when(workflowManagementService.register("1", name, new String(content))).thenReturn(expectedResponse);

        ResponseEntity<RegisterWorkflowResponse> response = controller.registerWorkflow("1", name, file);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(name);
        assertThat(response.getBody().getVersion()).isEqualTo(1);
        assertThat(response.getBody().getCreatedAt()).isNotNull();

        verify(workflowManagementService).register("1", name, new String(content));
    }

    @Test
    @DisplayName("registerWorkflow: happy path with application/x-yaml file")
    void registerWorkflow_happyPath_with_application_x_yaml_content_type() {
        String name = "invoice-processing";
        byte[] content = "name: invoice-processing".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "definition",
                "workflow.yaml",
                "application/x-yaml",
                content
        );

        when(workflowFileReader.read(file)).thenReturn(new String(content));
        RegisterWorkflowResponse expectedResponse = new RegisterWorkflowResponse();
        expectedResponse.setName(name);
        expectedResponse.setVersion(1);
        expectedResponse.setCreatedAt(OffsetDateTime.now());
        when(workflowManagementService.register("1", name, new String(content))).thenReturn(expectedResponse);

        ResponseEntity<RegisterWorkflowResponse> response = controller.registerWorkflow("1", name, file);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(name);
        assertThat(response.getBody().getVersion()).isEqualTo(1);
        assertThat(response.getBody().getCreatedAt()).isNotNull();

        verify(workflowManagementService).register("1", name, new String(content));
    }

    @Test
    @DisplayName("registerWorkflow: happy path with text/yaml file")
    void registerWorkflow_happyPath_with_text_yaml_content_type() {
        String name = "invoice-processing";
        byte[] content = "name: invoice-processing".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "definition",
                "workflow.yaml",
                "text/yaml",
                content
        );

        when(workflowFileReader.read(file)).thenReturn(new String(content));

        when(workflowFileReader.read(file)).thenReturn(new String(content));
        RegisterWorkflowResponse expectedResponse = new RegisterWorkflowResponse();
        expectedResponse.setName(name);
        expectedResponse.setVersion(1);
        expectedResponse.setCreatedAt(OffsetDateTime.now());
        when(workflowManagementService.register("1", name, new String(content))).thenReturn(expectedResponse);


        var response = controller.registerWorkflow("1", name, file);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(name);
        assertThat(response.getBody().getVersion()).isEqualTo(1);
        assertThat(response.getBody().getCreatedAt()).isNotNull();

        verify(workflowManagementService).register("1", name, new String(content));
    }

    @Test
    @DisplayName("registerWorkflow: empty file throws FileProcessingException")
    void registerWorkflow_emptyFile_throws() {
        String name = "invoice-processing";
        MockMultipartFile empty = new MockMultipartFile(
                "definition",
                "empty.yaml",
                "application/yaml",
                new byte[0]
        );

        assertThatThrownBy(() -> controller.registerWorkflow("1", name, empty))
                .isInstanceOf(FileProcessingException.class)
                .hasMessageContaining("No workflow file provided");
    }

    @Test
    @DisplayName("registerWorkflow: non-YAML content type throws UnsupportedMediaTypeException")
    void registerWorkflow_wrongContentType_throws() {
        String name = "invoice-processing";
        MockMultipartFile file = new MockMultipartFile(
                "definition",
                "workflow.txt",
                "text/plain",
                "content".getBytes()
        );

        assertThatThrownBy(() -> controller.registerWorkflow("1", name, file))
                .isInstanceOf(UnsupportedMediaTypeException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    @DisplayName("registerWorkflow: no content type throws UnsupportedMediaTypeException")
    void registerWorkflow_noContentType_throws() {
        String name = "invoice-processing";
        byte[] content = "name: invoice-processing".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "definition",
                "workflow.yaml",
                null,
                content
        );

        assertThatThrownBy(() -> controller.registerWorkflow("1", name, file))
                .isInstanceOf(UnsupportedMediaTypeException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    @DisplayName("listWorkflows: list all workflows for tenant")
    void listWorkflows() {
        ListWorkflowsResponse response = new ListWorkflowsResponse();
        WorkflowListItem item = new WorkflowListItem();
        item.setName("invoice-processing");
        item.setLatestVersion(1);
        response.setItems(List.of(item));
        when(workflowManagementService.listWorkflows("tenant-123", "curstor", 100)).thenReturn(response);

        ResponseEntity<ListWorkflowsResponse> result = controller.listWorkflows("tenant-123", "curstor", 100);
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("listWorkflowVersions: list all workflow versions")
    void listWorkflowVersions() {
        ListWorkflowVersionsResponse response = new ListWorkflowVersionsResponse();
        WorkflowVersionMetadata item1 = new WorkflowVersionMetadata();
        item1.setVersion(1);
        item1.setCreatedAt(OffsetDateTime.now().minusDays(1));

        WorkflowVersionMetadata item2 = new WorkflowVersionMetadata();
        item2.setVersion(2);
        item2.setCreatedAt(OffsetDateTime.now());
        response.setVersions(List.of(item1, item2));

        when(workflowManagementService.listWorkflowVersions("tenant-123", "workflow-1")).thenReturn(response);
        ResponseEntity<ListWorkflowVersionsResponse> result = controller.listWorkflowVersions("tenant-123", "workflow-1");
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("getWorkflowVersion: get details for Workflow version")
    void getWorkflowVersion() {
        WorkflowVersion response = new WorkflowVersion();
        response.setVersion(2);
        response.setCreatedAt(OffsetDateTime.now().minusDays(1));
        response.setName("workflow-1");

        when(workflowManagementService.getWorkflowVersion("tenant-123", "workflow-1", 2)).thenReturn(response);

        ResponseEntity<WorkflowVersion> result = controller.getWorkflowVersion("tenant-123", "workflow-1", 2);
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(response);
    }
}
