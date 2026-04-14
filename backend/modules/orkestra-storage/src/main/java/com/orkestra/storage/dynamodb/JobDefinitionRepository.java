package com.orkestra.storage.dynamodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.dsl.model.DslModel;
import com.orkestra.graph.model.GraphModel;
import com.orkestra.graph.model.GraphStepModel;
import com.orkestra.storage.dynamodb.exception.SaveJobException;
import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@AllArgsConstructor
public class JobDefinitionRepository {

    private final DynamoDbTable<JobDefinitionTable> dynamoDbTable;
    private final ObjectMapper objectMapper;

    public JobDefinitionTable getJobDefinition(String tenant, String name, Integer version) {
        JobDefinitionTable keyItem = new JobDefinitionTable();
        keyItem.setTenant(tenant);
        keyItem.setName(name);
        keyItem.setVersion(version);
        return dynamoDbTable.getItem(keyItem);
    }

    public void save(final String tenantId, final String name, final Integer version, OffsetDateTime offsetDateTime, GraphModel graphModel, DslModel dslModel) {
        final String definition = getDefinition(graphModel);

        final JobDefinitionTable table = new JobDefinitionTable();
        table.setTenant(tenantId);
        table.setName(name);
        table.setVersion(version);
        table.setCreatedAt(offsetDateTime);
        table.setDefinition(definition);
        table.setTopoOrder(graphModel.getSteps().stream().map(GraphStepModel::getId).toList());
        table.setEdges(graphModel.getEdges().stream().map(edge -> {
            Map<String, String> edgeMap = new HashMap<>();
            edgeMap.put(edge.getFrom(), edge.getTo());
            return edgeMap;
        }).toList());
        table.setGraphJson(getJson(graphModel));
        table.setDslJson(getJson(dslModel));

        // Only write if the graphModel (pk, sk) doesn't exist
        Map<String, String> names = new HashMap<>();
        names.put("#pk", "pk");
        names.put("#sk", "sk");

        String condition = "attribute_not_exists(#pk) AND attribute_not_exists(#sk)";

        Expression expression = Expression.builder()
                                          .expression(condition)
                                          .expressionNames(names)
                                          .build();

        PutItemEnhancedRequest<JobDefinitionTable> putItemRequest =
                PutItemEnhancedRequest.builder(JobDefinitionTable.class)
                                      .item(table)
                                      .conditionExpression(expression)
                                      .build();

        dynamoDbTable.putItem(putItemRequest);
    }

    private <T> String getJson(T model) {
        try {
            return objectMapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new SaveJobException("Error saving Workflow");
        }
    }

    private String getDefinition(GraphModel item) {
        return getJson(item);
    }

    public List<JobDefinitionTable> getJobDefinitionVersions(String tenantId, String name) {
        JobDefinitionTable keyItem = new JobDefinitionTable();
        keyItem.setTenant(tenantId);
        keyItem.setName(name);

        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(keyItem.getPk()).build()
        );

        QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                                                                   .queryConditional(conditional);
        QueryEnhancedRequest request = builder.build();

        Iterator<Page<JobDefinitionTable>> iterator = dynamoDbTable.query(request).iterator();
        List<JobDefinitionTable> items = new ArrayList<>();

        while (iterator.hasNext()) {
            Page<JobDefinitionTable> page = iterator.next();
            items.addAll(page.items());
        }
        // Sort by timestamp ascending (client-side, since timestamp is not the sort key)
        items.sort(Comparator.comparing(JobDefinitionTable::getVersion, Comparator.nullsLast(Comparator.naturalOrder())));
        return items;
    }
}
