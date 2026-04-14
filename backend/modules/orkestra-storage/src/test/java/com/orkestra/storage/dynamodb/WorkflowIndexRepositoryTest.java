package com.orkestra.storage.dynamodb;

import com.orkestra.storage.dynamodb.model.WorkflowIndexTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowIndexRepositoryTest {

    @Mock
    private DynamoDbTable<WorkflowIndexTable> workflowIndexTable;

    private WorkflowIndexRepository repository;

    @BeforeEach
    void setUp() {
        repository = new WorkflowIndexRepository(workflowIndexTable);
    }

    @Test
    void getWorkflowIndex_shouldReturnItem_whenKeyExists() {
        String tenantId = "tenant1";
        String workflowName = "workflowA";

        WorkflowIndexTable expectedItem = new WorkflowIndexTable();
        expectedItem.setTenantId(tenantId);
        expectedItem.setWorkflowName(workflowName);
        expectedItem.setLatestVersion(1);

        when(workflowIndexTable.getItem(any(WorkflowIndexTable.class))).thenReturn(expectedItem);

        WorkflowIndexTable result = repository.getWorkflowIndex(tenantId, workflowName);

        assertNotNull(result);
        assertEquals(expectedItem, result);
        assertEquals(tenantId, result.getTenantId());
        assertEquals(workflowName, result.getWorkflowName());

        ArgumentCaptor<WorkflowIndexTable> captor = ArgumentCaptor.forClass(WorkflowIndexTable.class);
        verify(workflowIndexTable).getItem(captor.capture());

        WorkflowIndexTable keyItem = captor.getValue();
        assertEquals(tenantId, keyItem.getTenantId());
        assertEquals(workflowName, keyItem.getWorkflowName());
    }

    @Test
    void getWorkflowIndex_shouldReturnNull_whenItemDoesNotExist() {
        when(workflowIndexTable.getItem(any(WorkflowIndexTable.class))).thenReturn(null);

        WorkflowIndexTable result = repository.getWorkflowIndex("tenant2", "workflowB");

        assertNull(result);
        verify(workflowIndexTable).getItem(any(WorkflowIndexTable.class));
    }

    @Test
    void getWorkflowIndex_shouldReturnNull_whenInputsAreNull() {
        assertNull(repository.getWorkflowIndex(null, "workflowC"));
        assertNull(repository.getWorkflowIndex("tenant3", null));

        verify(workflowIndexTable, never()).getItem(any(WorkflowIndexTable.class));
    }

    @Test
    void listWorkflowIndexes_shouldReturnEmptyResult_whenNoPagesExist() {
        @SuppressWarnings("unchecked")
        PageIterable<WorkflowIndexTable> iterable = mock(PageIterable.class);
        Iterator<Page<WorkflowIndexTable>> iterator = List.<Page<WorkflowIndexTable>>of().iterator();

        when(workflowIndexTable.query(any(QueryEnhancedRequest.class))).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(iterator);

        WorkflowIndexRepository.PageResult<WorkflowIndexTable> result =
                repository.listWorkflowIndexes("tenant1", 10, null, null);

        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertNull(result.nextPk());
        assertNull(result.nextSk());

        verify(workflowIndexTable).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void listWorkflowIndexes_shouldReturnItemsWithoutCursor_whenLastEvaluatedKeyIsNull() {
        WorkflowIndexTable item1 = new WorkflowIndexTable();
        item1.setTenantId("tenant1");
        item1.setWorkflowName("workflowA");
        item1.setLatestVersion(1);

        WorkflowIndexTable item2 = new WorkflowIndexTable();
        item2.setTenantId("tenant1");
        item2.setWorkflowName("workflowB");
        item2.setLatestVersion(2);

        @SuppressWarnings("unchecked")
        Page<WorkflowIndexTable> page = mock(Page.class);
        when(page.items()).thenReturn(List.of(item1, item2));
        when(page.lastEvaluatedKey()).thenReturn(null);

        @SuppressWarnings("unchecked")
        PageIterable<WorkflowIndexTable> iterable = mock(PageIterable.class);
        when(workflowIndexTable.query(any(QueryEnhancedRequest.class))).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(List.of(page).iterator());

        WorkflowIndexRepository.PageResult<WorkflowIndexTable> result =
                repository.listWorkflowIndexes("tenant1", 25, null, null);

        assertNotNull(result);
        assertEquals(2, result.items().size());
        assertNull(result.nextPk());
        assertNull(result.nextSk());

        ArgumentCaptor<QueryEnhancedRequest> captor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(workflowIndexTable).query(captor.capture());

        QueryEnhancedRequest request = captor.getValue();
        assertNotNull(request);
        assertNotNull(request.queryConditional());
    }

    @Test
    void listWorkflowIndexes_shouldReturnCursor_whenLastEvaluatedKeyExists() {
        WorkflowIndexTable item = new WorkflowIndexTable();
        item.setTenantId("tenant1");
        item.setWorkflowName("workflowA");
        item.setLatestVersion(1);

        Map<String, AttributeValue> lastEvaluatedKey = Map.of(
                "tenantId", AttributeValue.builder().s("tenant1").build(),
                "workflowName", AttributeValue.builder().s("workflowA").build()
        );

        @SuppressWarnings("unchecked")
        Page<WorkflowIndexTable> page = mock(Page.class);
        when(page.items()).thenReturn(List.of(item));
        when(page.lastEvaluatedKey()).thenReturn(lastEvaluatedKey);

        @SuppressWarnings("unchecked")
        PageIterable<WorkflowIndexTable> iterable = mock(PageIterable.class);
        when(workflowIndexTable.query(any(QueryEnhancedRequest.class))).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(List.of(page).iterator());

        WorkflowIndexRepository.PageResult<WorkflowIndexTable> result =
                repository.listWorkflowIndexes("tenant1", 25, null, null);

        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals("tenant1", result.nextPk());
        assertEquals("workflowA", result.nextSk());
    }

    @Test
    void listWorkflowIndexes_shouldApplyLimitAndExclusiveStartKey_whenProvided() {
        WorkflowIndexTable item = new WorkflowIndexTable();
        item.setTenantId("tenant1");
        item.setWorkflowName("workflowB");

        @SuppressWarnings("unchecked")
        Page<WorkflowIndexTable> page = mock(Page.class);
        when(page.items()).thenReturn(List.of(item));
        when(page.lastEvaluatedKey()).thenReturn(null);

        @SuppressWarnings("unchecked")
        PageIterable<WorkflowIndexTable> iterable = mock(PageIterable.class);
        when(workflowIndexTable.query(any(QueryEnhancedRequest.class))).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(List.of(page).iterator());

        repository.listWorkflowIndexes("tenant1", 50, "tenant1", "workflowA");

        ArgumentCaptor<QueryEnhancedRequest> captor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(workflowIndexTable).query(captor.capture());

        QueryEnhancedRequest request = captor.getValue();
        assertNotNull(request);
        assertEquals(50, request.limit());
        assertNotNull(request.exclusiveStartKey());
        assertEquals("tenant1", request.exclusiveStartKey().get("tenantId").s());
        assertEquals("workflowA", request.exclusiveStartKey().get("workflowName").s());
    }

    @Test
    void listWorkflowIndexes_withoutPageArguments_shouldReturnItemsOnly() {
        WorkflowIndexTable item = new WorkflowIndexTable();
        item.setTenantId("tenant1");
        item.setWorkflowName("workflowA");

        @SuppressWarnings("unchecked")
        Page<WorkflowIndexTable> page = mock(Page.class);
        when(page.items()).thenReturn(List.of(item));
        when(page.lastEvaluatedKey()).thenReturn(null);

        @SuppressWarnings("unchecked")
        PageIterable<WorkflowIndexTable> iterable = mock(PageIterable.class);
        when(workflowIndexTable.query(any(QueryEnhancedRequest.class))).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(List.of(page).iterator());

        List<WorkflowIndexTable> result = repository.listWorkflowIndexes("tenant1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("workflowA", result.get(0).getWorkflowName());
    }

    @Test
    void save_shouldPersistExpectedItemWithConditionExpression() {
        OffsetDateTime timestamp = OffsetDateTime.parse("2026-04-13T21:30:00Z");

        repository.save("tenant1", "workflowA", timestamp, 3);

        ArgumentCaptor<PutItemEnhancedRequest<WorkflowIndexTable>> captor =
                ArgumentCaptor.forClass(PutItemEnhancedRequest.class);

        verify(workflowIndexTable).putItem(captor.capture());

        PutItemEnhancedRequest<WorkflowIndexTable> request = captor.getValue();
        WorkflowIndexTable item = request.item();
        Expression expression = request.conditionExpression();

        assertNotNull(item);
        assertEquals("tenant1", item.getTenantId());
        assertEquals("workflowA", item.getWorkflowName());
        assertEquals(3, item.getLatestVersion());
        assertEquals(timestamp, item.getTimestamp());

        assertNotNull(expression);
        assertEquals(
                "(attribute_not_exists(#tenantId) AND attribute_not_exists(#workflowName)) OR (#latestVersion = :expected)",
                expression.expression()
        );
        assertEquals(
                Map.of(
                        "#tenantId", "tenantId",
                        "#workflowName", "workflowName",
                        "#latestVersion", "latestVersion"
                ),
                expression.expressionNames()
        );
        assertNotNull(expression.expressionValues());
        assertEquals("2", expression.expressionValues().get(":expected").n());
    }

    @Test
    void save_shouldBuildExpressionWithoutExpectedValue_whenLatestVersionIsNull() {
        OffsetDateTime timestamp = OffsetDateTime.parse("2026-04-13T21:30:00Z");

        repository.save("tenant1", "workflowA", timestamp, null);

        ArgumentCaptor<PutItemEnhancedRequest<WorkflowIndexTable>> captor =
                ArgumentCaptor.forClass(PutItemEnhancedRequest.class);

        verify(workflowIndexTable).putItem(captor.capture());

        PutItemEnhancedRequest<WorkflowIndexTable> request = captor.getValue();
        Expression expression = request.conditionExpression();

        assertNotNull(expression);
        assertEquals(
                "(attribute_not_exists(#tenantId) AND attribute_not_exists(#workflowName))",
                expression.expression()
        );
        assertEquals(
                Map.of(
                        "#tenantId", "tenantId",
                        "#workflowName", "workflowName",
                        "#latestVersion", "latestVersion"
                ),
                expression.expressionNames()
        );
        assertNull(expression.expressionValues());
    }
}
