package com.orkestra.storage.dynamodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.dsl.model.DslModel;
import com.orkestra.graph.model.GraphEdgeModel;
import com.orkestra.graph.model.GraphModel;
import com.orkestra.graph.model.GraphStepModel;
import com.orkestra.storage.dynamodb.exception.SaveJobException;
import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobDefinitionRepositoryTest {

    @Mock
    private DynamoDbTable<JobDefinitionTable> dynamoDbTable;

    @Mock
    private ObjectMapper objectMapper;

    private JobDefinitionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JobDefinitionRepository(dynamoDbTable, objectMapper);
    }

    @Test
    void getJobDefinition_shouldBuildKeyAndReturnItem() {
        JobDefinitionTable expected = new JobDefinitionTable();
        expected.setTenant("tenant-1");
        expected.setName("invoice-processing");
        expected.setVersion(2);

        when(dynamoDbTable.getItem(any(JobDefinitionTable.class))).thenReturn(expected);

        JobDefinitionTable result = repository.getJobDefinition("tenant-1", "invoice-processing", 2);

        assertNotNull(result);
        assertEquals(expected, result);

        ArgumentCaptor<JobDefinitionTable> captor = ArgumentCaptor.forClass(JobDefinitionTable.class);
        verify(dynamoDbTable).getItem(captor.capture());

        JobDefinitionTable keyItem = captor.getValue();
        assertEquals("tenant-1", keyItem.getTenant());
        assertEquals("invoice-processing", keyItem.getName());
        assertEquals(2, keyItem.getVersion());
        assertEquals("TENANT#tenant-1#WF#invoice-processing", keyItem.getPk());
        assertEquals("VERSION#2", keyItem.getSk());
    }

    @Test
    void save_shouldPersistExpectedItem() throws Exception {
        String tenant = "tenant-1";
        String name = "invoice-processing";
        Integer version = 3;
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-04-13T21:30:00Z");

        GraphStepModel step1 = new GraphStepModel();
        step1.setId("validate");
        step1.setType("noop");

        GraphStepModel step2 = new GraphStepModel();
        step2.setId("persist");
        step2.setType("http");

        GraphEdgeModel edge = new GraphEdgeModel();
        edge.setFrom("validate");
        edge.setTo("persist");

        GraphModel graphModel = new GraphModel();
        graphModel.setSteps(List.of(step1, step2));
        graphModel.setEdges(List.of(edge));

        DslModel dslModel = mock(DslModel.class);

        when(objectMapper.writeValueAsString(graphModel))
                .thenReturn("{graph-definition}")
                .thenReturn("{graph-json}");
        when(objectMapper.writeValueAsString(dslModel))
                .thenReturn("{dsl-json}");

        repository.save(tenant, name, version, createdAt, graphModel, dslModel);

        ArgumentCaptor<PutItemEnhancedRequest<JobDefinitionTable>> captor =
                ArgumentCaptor.forClass(PutItemEnhancedRequest.class);

        verify(dynamoDbTable).putItem(captor.capture());

        PutItemEnhancedRequest<JobDefinitionTable> request = captor.getValue();
        JobDefinitionTable saved = request.item();

        assertNotNull(saved);
        assertEquals(tenant, saved.getTenant());
        assertEquals(name, saved.getName());
        assertEquals(version, saved.getVersion());
        assertEquals(createdAt, saved.getCreatedAt());

        assertEquals("TENANT#tenant-1#WF#invoice-processing", saved.getPk());
        assertEquals("VERSION#3", saved.getSk());

        assertEquals("{graph-definition}", saved.getDefinition());
        assertEquals("{graph-json}", saved.getGraphJson());
        assertEquals("{dsl-json}", saved.getDslJson());

        assertEquals(List.of("validate", "persist"), saved.getTopoOrder());
        assertEquals(1, saved.getEdges().size());
        assertEquals(Map.of("validate", "persist"), saved.getEdges().get(0));

        assertNotNull(request.conditionExpression());
        assertEquals("attribute_not_exists(#pk) AND attribute_not_exists(#sk)",
                     request.conditionExpression().expression());
        assertEquals(Map.of("#pk", "pk", "#sk", "sk"),
                     request.conditionExpression().expressionNames());
    }

    @Test
    void save_shouldThrowSaveJobException_whenGraphSerializationFails() throws Exception {
        GraphModel graphModel = mock(GraphModel.class);
        DslModel dslModel = mock(DslModel.class);

        when(objectMapper.writeValueAsString(graphModel))
                .thenThrow(new JsonProcessingException("boom") {});

        SaveJobException ex = assertThrows(
                SaveJobException.class,
                () -> repository.save("tenant-1", "wf", 1, OffsetDateTime.now(), graphModel, dslModel)
        );

        assertEquals("Error saving Workflow", ex.getMessage());
        verify(dynamoDbTable, never()).putItem(any(PutItemEnhancedRequest.class));
    }

    @Test
    void save_shouldThrowSaveJobException_whenDslSerializationFails() throws Exception {
        GraphStepModel step = new GraphStepModel();
        step.setId("validate");
        step.setType("noop");

        GraphModel graphModel = new GraphModel();
        graphModel.setSteps(List.of(step));
        graphModel.setEdges(List.of());

        DslModel dslModel = mock(DslModel.class);

        when(objectMapper.writeValueAsString(graphModel))
                .thenReturn("{graph-definition}")
                .thenReturn("{graph-json}");
        when(objectMapper.writeValueAsString(dslModel))
                .thenThrow(new JsonProcessingException("boom") {});

        SaveJobException ex = assertThrows(
                SaveJobException.class,
                () -> repository.save("tenant-1", "wf", 1, OffsetDateTime.now(), graphModel, dslModel)
        );

        assertEquals("Error saving Workflow", ex.getMessage());
        verify(dynamoDbTable, never()).putItem(any(PutItemEnhancedRequest.class));
    }

    @Test
    void getJobDefinitionVersions_shouldReturnAllItemsSortedByVersionAscending() {
        JobDefinitionTable v3 = new JobDefinitionTable();
        v3.setTenant("tenant-1");
        v3.setName("invoice-processing");
        v3.setVersion(3);

        JobDefinitionTable v1 = new JobDefinitionTable();
        v1.setTenant("tenant-1");
        v1.setName("invoice-processing");
        v1.setVersion(1);

        JobDefinitionTable v2 = new JobDefinitionTable();
        v2.setTenant("tenant-1");
        v2.setName("invoice-processing");
        v2.setVersion(2);

        @SuppressWarnings("unchecked")
        Page<JobDefinitionTable> page1 = mock(Page.class);
        when(page1.items()).thenReturn(List.of(v3, v1));

        @SuppressWarnings("unchecked")
        Page<JobDefinitionTable> page2 = mock(Page.class);
        when(page2.items()).thenReturn(List.of(v2));

        @SuppressWarnings("unchecked")
        PageIterable<JobDefinitionTable> sdkIterable = mock(PageIterable.class);

        when(dynamoDbTable.query(any(QueryEnhancedRequest.class))).thenReturn(sdkIterable);
        when(sdkIterable.iterator()).thenReturn(List.of(page1, page2).iterator());

        List<JobDefinitionTable> result =
                repository.getJobDefinitionVersions("tenant-1", "invoice-processing");

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getVersion());
        assertEquals(2, result.get(1).getVersion());
        assertEquals(3, result.get(2).getVersion());

        ArgumentCaptor<QueryEnhancedRequest> captor =
                ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(dynamoDbTable).query(captor.capture());

        QueryEnhancedRequest request = captor.getValue();
        assertNotNull(request);
        assertNotNull(request.queryConditional());
    }

    @Test
    void getJobDefinitionVersions_shouldReturnEmptyList_whenNoPagesReturned() {
        @SuppressWarnings("unchecked")
        PageIterable<JobDefinitionTable> sdkIterable = mock(PageIterable.class);

        when(dynamoDbTable.query(any(QueryEnhancedRequest.class))).thenReturn(sdkIterable);
        when(sdkIterable.iterator()).thenReturn(List.<Page<JobDefinitionTable>>of().iterator());

        List<JobDefinitionTable> result =
                repository.getJobDefinitionVersions("tenant-1", "invoice-processing");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
