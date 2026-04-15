package com.orkestra.storage.dynamodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.dsl.model.DslModel;
import com.orkestra.graph.model.GraphModel;
import com.orkestra.graph.model.GraphStepModel;
import com.orkestra.storage.dynamodb.exception.SaveJobException;
import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import com.orkestra.storage.dynamodb.model.PlatformAdminTable;
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
public class PlatformAdminRepository {

    private final DynamoDbTable<PlatformAdminTable> dynamoDbTable;
    private final ObjectMapper objectMapper;

    public PlatformAdminTable getPlatformAdminUser(String userId) {
        PlatformAdminTable keyItem = new PlatformAdminTable();
        keyItem.setUserId(userId);
        return dynamoDbTable.getItem(keyItem);
    }

    public boolean isPlatformAdmin(String userId) {
        return getPlatformAdminUser(userId) != null;
    }

}
