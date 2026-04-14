package com.orkestra.storage.dynamodb;

import com.orkestra.storage.dynamodb.model.WorkflowIndexTable;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class WorkflowIndexRepository {

    private final DynamoDbTable<WorkflowIndexTable> workflowIndexTable;

    public WorkflowIndexTable getWorkflowIndex(String tenant, String name) {
        if (tenant == null || name == null) {
            return null;
        }
        WorkflowIndexTable keyItem = new WorkflowIndexTable();
        keyItem.setTenantId(tenant);
        keyItem.setWorkflowName(name);
        return workflowIndexTable.getItem(keyItem);
    }

    public List<WorkflowIndexTable> listWorkflowIndexes(String tenant) {
        return listWorkflowIndexes(tenant, null, null, null).items();
    }

    public PageResult<WorkflowIndexTable> listWorkflowIndexes(String tenant, Integer pageSize, String paginationPk, String paginationSk) {
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(tenant).build()
        );

        final Map<String, AttributeValue> pagination;
        if (paginationPk != null && paginationSk != null) {
            pagination = Map.of(
                    "tenantId", AttributeValue.builder().s(paginationPk).build(),
                    "workflowName", AttributeValue.builder().s(paginationSk).build()
            );
        } else {
            pagination = null;
        }

        QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                                                                   .queryConditional(conditional);
        if (pageSize != null && pageSize > 0) {
            builder.limit(pageSize);
        }
        if (pagination != null) {
            builder.exclusiveStartKey(pagination);
        }

        var request = builder.build();

        var iterator = workflowIndexTable.query(request).iterator();
        if (!iterator.hasNext()) {
            return new PageResult<>(List.of(), null, null);
        }

        var page = iterator.next();
        List<WorkflowIndexTable> items = new ArrayList<>(page.items());

        Map<String, AttributeValue> nextKey = page.lastEvaluatedKey();
        final String nextPk = nextKey != null ? nextKey.get("tenantId").s() : null;
        final String nextSk = nextKey != null ? nextKey.get("workflowName").s() : null;
        return new PageResult<>(items, nextPk, nextSk);
    }

    public void save(String tenant, String name, OffsetDateTime offsetDateTime, Integer latestVersion) {
        WorkflowIndexTable item = new WorkflowIndexTable();
        item.setTenantId(tenant);
        item.setWorkflowName(name);
        item.setLatestVersion(latestVersion);
        item.setTimestamp(offsetDateTime);

        // Condition: write only if item doesn't exist OR existing latestVersion == (newVersion - 1)
        Map<String, String> names = new HashMap<>();
        names.put("#tenantId", "tenantId");
        names.put("#workflowName", "workflowName");
        names.put("#latestVersion", "latestVersion");

        Map<String, AttributeValue> values = new HashMap<>();
        if (latestVersion != null) {
            values.put(":expected", AttributeValue.builder().n(String.valueOf(latestVersion - 1)).build());
        }

        String condition =
                "(attribute_not_exists(#tenantId) AND attribute_not_exists(#workflowName))" +
                        (latestVersion != null ? " OR (#latestVersion = :expected)" : "");

        Expression expression = Expression.builder()
                                          .expression(condition)
                                          .expressionNames(names)
                                          .expressionValues(values.isEmpty() ? null : values)
                                          .build();

        PutItemEnhancedRequest<WorkflowIndexTable> put =
                PutItemEnhancedRequest.builder(WorkflowIndexTable.class)
                                      .item(item)
                                      .conditionExpression(expression)
                                      .build();

        workflowIndexTable.putItem(put);

    }

    public record PageResult<T>(List<T> items, String nextPk, String nextSk) {
    }
}
