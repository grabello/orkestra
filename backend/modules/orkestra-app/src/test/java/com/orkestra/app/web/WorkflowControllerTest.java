package com.orkestra.app.web;

import com.orkestra.app.service.WorkflowRegistrationService;
import com.orkestra.app.web.util.WorkflowFileReader;
import com.orkestra.exception.FileProcessingException;
import com.orkestra.exception.UnsupportedMediaTypeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

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
    private WorkflowRegistrationService workflowRegistrationService;

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

        var response = controller.registerWorkflow(name, file);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(name);
        assertThat(response.getBody().getVersion()).isEqualTo(1);
        assertThat(response.getBody().getCreatedAt()).isNotNull();

        verify(workflowRegistrationService).register(name, new String(content));
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

        var response = controller.registerWorkflow(name, file);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(name);
        assertThat(response.getBody().getVersion()).isEqualTo(1);
        assertThat(response.getBody().getCreatedAt()).isNotNull();

        verify(workflowRegistrationService).register(name, new String(content));
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

        var response = controller.registerWorkflow(name, file);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(name);
        assertThat(response.getBody().getVersion()).isEqualTo(1);
        assertThat(response.getBody().getCreatedAt()).isNotNull();

        verify(workflowRegistrationService).register(name, new String(content));
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

        assertThatThrownBy(() -> controller.registerWorkflow(name, empty))
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

        assertThatThrownBy(() -> controller.registerWorkflow(name, file))
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

        assertThatThrownBy(() -> controller.registerWorkflow(name, file))
                .isInstanceOf(UnsupportedMediaTypeException.class)
                .hasMessageContaining("Unsupported file type");
    }
}
