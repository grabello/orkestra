package com.orkestra.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.api.model.Dsl;
import com.orkestra.api.model.Graph;
import com.orkestra.api.model.ListWorkflowVersionsResponse;
import com.orkestra.api.model.ListWorkflowsResponse;
import com.orkestra.api.model.RegisterWorkflowResponse;
import com.orkestra.api.model.WorkflowVersion;
import com.orkestra.api.model.WorkflowVersionMetadata;
import com.orkestra.app.converter.ModelConverter;
import com.orkestra.app.cursor.CursorCodec;
import com.orkestra.app.cursor.WorkflowCursor;
import com.orkestra.dsl.model.DslModel;
import com.orkestra.dsl.validator.WorkflowDslValidator;
import com.orkestra.dsl.validator.WorkflowYamlValidator;
import com.orkestra.exception.WorkflowNotFoundException;
import com.orkestra.graph.model.GraphModel;
import com.orkestra.storage.dynamodb.JobDefinitionRepository;
import com.orkestra.storage.dynamodb.WorkflowIndexRepository;
import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import com.orkestra.storage.dynamodb.model.WorkflowIndexTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowManagementServiceTest {

    @Mock
    private WorkflowYamlValidator workflowYamlValidator;

    @Mock
    private WorkflowDslValidator workflowDslValidator;

    @Mock
    private WorkflowIndexRepository workflowIndexRepository;

    @Mock
    private JobDefinitionRepository jobDefinitionRepository;

    @Mock
    private ModelConverter modelConverter;

    @Mock
    private CursorCodec cursorCodec;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WorkflowManagementService workflowManagementService;

    private DslModel dslModel;
    private GraphModel graphModel;

    @BeforeEach
    void setUp() {
        dslModel = mock(DslModel.class);
        graphModel = mock(GraphModel.class);
    }

    @Test
    void register_shouldCreateVersionOne_whenWorkflowDoesNotExist() {
        String tenant = "tenant-1";
        String name = "invoice-processing";
        String yaml = "name: invoice-processing";

        when(workflowYamlValidator.validate(name, yaml)).thenReturn(dslModel);
        when(workflowDslValidator.validate(dslModel)).thenReturn(graphModel);
        when(workflowIndexRepository.getWorkflowIndex(tenant, name)).thenReturn(null);

        RegisterWorkflowResponse response = workflowManagementService.register(tenant, name, yaml);

        assertNotNull(response);
        assertEquals(name, response.getName());
        assertEquals(1, response.getVersion());
        assertNotNull(response.getCreatedAt());

        verify(workflowYamlValidator).validate(name, yaml);
        verify(workflowDslValidator).validate(dslModel);

        verify(jobDefinitionRepository).save(
                eq(tenant),
                eq(name),
                eq(1),
                any(OffsetDateTime.class),
                eq(graphModel),
                eq(dslModel)
        );

        verify(workflowIndexRepository).save(
                eq(tenant),
                eq(name),
                any(OffsetDateTime.class),
                eq(1)
        );
    }

    @Test
    void register_shouldIncrementVersion_whenWorkflowAlreadyExists() {
        String tenant = "tenant-1";
        String name = "invoice-processing";
        String yaml = "name: invoice-processing";

        WorkflowIndexTable workflowIndex = mock(WorkflowIndexTable.class);

        when(workflowYamlValidator.validate(name, yaml)).thenReturn(dslModel);
        when(workflowDslValidator.validate(dslModel)).thenReturn(graphModel);
        when(workflowIndexRepository.getWorkflowIndex(tenant, name)).thenReturn(workflowIndex);
        when(workflowIndex.getLatestVersion()).thenReturn(7);

        RegisterWorkflowResponse response = workflowManagementService.register(tenant, name, yaml);

        assertNotNull(response);
        assertEquals(name, response.getName());
        assertEquals(8, response.getVersion());

        verify(jobDefinitionRepository).save(
                eq(tenant),
                eq(name),
                eq(8),
                any(OffsetDateTime.class),
                eq(graphModel),
                eq(dslModel)
        );

        verify(workflowIndexRepository).save(
                eq(tenant),
                eq(name),
                any(OffsetDateTime.class),
                eq(8)
        );
    }

    @Test
    void register_shouldUseCurrentTenant_whenLookingUpWorkflowIndex() {
        String tenant = "tenant-123";
        String name = "invoice-processing";
        String yaml = "name: invoice-processing";

        when(workflowYamlValidator.validate(name, yaml)).thenReturn(dslModel);
        when(workflowDslValidator.validate(dslModel)).thenReturn(graphModel);
        when(workflowIndexRepository.getWorkflowIndex(anyString(), eq(name))).thenReturn(null);

        workflowManagementService.register(tenant, name, yaml);

        // This is the behavior you probably WANT.
        // Right now your implementation uses "1", so this test will fail until fixed.
        verify(workflowIndexRepository).getWorkflowIndex(tenant, name);
    }

    @Test
    void getWorkflowVersion_shouldReturnWorkflowVersion_whenWorkflowExists() throws Exception {
        String tenant = "tenant-1";
        String name = "invoice-processing";
        Integer version = 3;

        JobDefinitionTable jobDefinition = mock(JobDefinitionTable.class);
        Dsl apiDsl = new Dsl();
        Graph apiGraph = new Graph();
        OffsetDateTime createdAt = OffsetDateTime.now();

        when(jobDefinitionRepository.getJobDefinition(tenant, name, version)).thenReturn(jobDefinition);
        when(jobDefinition.getGraphJson()).thenReturn("{\"steps\":[],\"edges\":[]}");
        when(jobDefinition.getDslJson()).thenReturn("{\"name\":\"invoice-processing\",\"steps\":[]}");
        when(jobDefinition.getCreatedAt()).thenReturn(createdAt);

        when(objectMapper.readValue(jobDefinition.getGraphJson(), GraphModel.class)).thenReturn(graphModel);
        when(objectMapper.readValue(jobDefinition.getDslJson(), DslModel.class)).thenReturn(dslModel);

        when(modelConverter.fromModel(dslModel)).thenReturn(apiDsl);
        when(modelConverter.fromModel(graphModel)).thenReturn(apiGraph);

        WorkflowVersion response = workflowManagementService.getWorkflowVersion(tenant, name, version);

        assertNotNull(response);
        assertEquals(name, response.getName());
        assertEquals(version, response.getVersion());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(apiDsl, response.getDsl());
        assertEquals(apiGraph, response.getGraph());

        verify(jobDefinitionRepository).getJobDefinition(tenant, name, version);
        verify(objectMapper).readValue(jobDefinition.getGraphJson(), GraphModel.class);
        verify(objectMapper).readValue(jobDefinition.getDslJson(), DslModel.class);
        verify(modelConverter).fromModel(dslModel);
        verify(modelConverter).fromModel(graphModel);
    }

    @Test
    void getWorkflowVersion_shouldThrowNotFound_whenWorkflowDoesNotExist() {
        String tenant = "tenant-1";
        String name = "invoice-processing";
        Integer version = 99;

        when(jobDefinitionRepository.getJobDefinition(tenant, name, version)).thenReturn(null);

        WorkflowNotFoundException ex = assertThrows(
                WorkflowNotFoundException.class,
                () -> workflowManagementService.getWorkflowVersion(tenant, name, version)
        );

        assertEquals("WORKFLOW_NOT_FOUND", ex.getCode());
        assertTrue(ex.getMessage().contains(name));
        assertTrue(ex.getMessage().contains(version.toString()));

        verify(jobDefinitionRepository).getJobDefinition(tenant, name, version);
        verifyNoInteractions(objectMapper, modelConverter);
    }

    @Test
    void getWorkflowVersion_shouldThrowIllegalStateException_whenGraphJsonCannotBeParsed() throws Exception {
        String tenant = "tenant-1";
        String name = "invoice-processing";
        Integer version = 1;

        JobDefinitionTable jobDefinition = mock(JobDefinitionTable.class);

        when(jobDefinitionRepository.getJobDefinition(tenant, name, version)).thenReturn(jobDefinition);
        when(jobDefinition.getGraphJson()).thenReturn("{invalid json}");
        when(objectMapper.readValue(jobDefinition.getGraphJson(), GraphModel.class))
                .thenThrow(new JsonProcessingException("boom") {});

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> workflowManagementService.getWorkflowVersion(tenant, name, version)
        );

        assertEquals("Failed to parse json", ex.getMessage());
    }

    @Test
    void listWorkflows_shouldUseEmptyCursor_whenCursorIsNull() {
        String tenant = "tenant-1";
        int limit = 25;

        WorkflowIndexRepository.PageResult<WorkflowIndexTable> pageResult = mock(WorkflowIndexRepository.PageResult.class);
        ListWorkflowsResponse expectedResponse = new ListWorkflowsResponse();

        when(workflowIndexRepository.listWorkflowIndexes(tenant, limit, null, null)).thenReturn(pageResult);
        when(modelConverter.fromPageResult(pageResult)).thenReturn(expectedResponse);

        ListWorkflowsResponse actual = workflowManagementService.listWorkflows(tenant, null, limit);

        assertSame(expectedResponse, actual);
        verifyNoInteractions(cursorCodec);
        verify(workflowIndexRepository).listWorkflowIndexes(tenant, limit, null, null);
        verify(modelConverter).fromPageResult(pageResult);
    }

    @Test
    void listWorkflows_shouldUseEmptyCursor_whenCursorIsBlank() {
        String tenant = "tenant-1";
        int limit = 25;

        WorkflowIndexRepository.PageResult<WorkflowIndexTable> pageResult = mock(WorkflowIndexRepository.PageResult.class);
        ListWorkflowsResponse expectedResponse = new ListWorkflowsResponse();

        when(workflowIndexRepository.listWorkflowIndexes(tenant, limit, null, null)).thenReturn(pageResult);
        when(modelConverter.fromPageResult(pageResult)).thenReturn(expectedResponse);

        ListWorkflowsResponse actual = workflowManagementService.listWorkflows(tenant, "", limit);

        assertSame(expectedResponse, actual);
        verifyNoInteractions(cursorCodec);
        verify(workflowIndexRepository).listWorkflowIndexes(tenant, limit, null, null);
        verify(modelConverter).fromPageResult(pageResult);
    }

    @Test
    void listWorkflows_shouldDecodeCursor_whenCursorIsProvided() {
        String tenant = "tenant-1";
        String cursor = "encoded-cursor";
        int limit = 25;

        WorkflowCursor workflowCursor = new WorkflowCursor(tenant, "TENANT#tenant-1", "WF#invoice-processing");
        WorkflowIndexRepository.PageResult<WorkflowIndexTable> pageResult = mock(WorkflowIndexRepository.PageResult.class);
        ListWorkflowsResponse expectedResponse = new ListWorkflowsResponse();

        when(cursorCodec.decode(cursor)).thenReturn(workflowCursor);
        when(workflowIndexRepository.listWorkflowIndexes(tenant, limit, workflowCursor.pk(), workflowCursor.sk()))
                .thenReturn(pageResult);
        when(modelConverter.fromPageResult(pageResult)).thenReturn(expectedResponse);

        ListWorkflowsResponse actual = workflowManagementService.listWorkflows(tenant, cursor, limit);

        assertSame(expectedResponse, actual);
        verify(cursorCodec).decode(cursor);
        verify(workflowIndexRepository).listWorkflowIndexes(tenant, limit, workflowCursor.pk(), workflowCursor.sk());
        verify(modelConverter).fromPageResult(pageResult);
    }

    @Test
    void listWorkflowVersions_shouldReturnConvertedMetadata() {
        String tenant = "tenant-1";
        String name = "invoice-processing";

        JobDefinitionTable version1 = mock(JobDefinitionTable.class);
        JobDefinitionTable version2 = mock(JobDefinitionTable.class);
        WorkflowVersionMetadata metadata1 = new WorkflowVersionMetadata();
        WorkflowVersionMetadata metadata2 = new WorkflowVersionMetadata();

        when(jobDefinitionRepository.getJobDefinitionVersions(tenant, name)).thenReturn(List.of(version1, version2));
        when(modelConverter.fromTableResult(version1)).thenReturn(metadata1);
        when(modelConverter.fromTableResult(version2)).thenReturn(metadata2);

        ListWorkflowVersionsResponse response = workflowManagementService.listWorkflowVersions(tenant, name);

        assertNotNull(response);
        assertEquals(name, response.getName());
        assertEquals(2, response.getVersions().size());
        assertEquals(List.of(metadata1, metadata2), response.getVersions());

        verify(jobDefinitionRepository).getJobDefinitionVersions(tenant, name);
        verify(modelConverter).fromTableResult(version1);
        verify(modelConverter).fromTableResult(version2);
    }
}
