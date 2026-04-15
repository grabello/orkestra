package com.orkestra.storage.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.storage.dynamodb.model.TenantMembershipTable;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;


@AllArgsConstructor
public class TenantMembershipRepository {

    private final DynamoDbTable<TenantMembershipTable> dynamoDbTable;
    private final ObjectMapper objectMapper;

    public TenantMembershipTable getTenantMembership(String tenantId, String userId) {
        TenantMembershipTable keyItem = new TenantMembershipTable();
        keyItem.setTenantId(tenantId);
        keyItem.setUserId(userId);
        return dynamoDbTable.getItem(keyItem);
    }

    public void save(String tenantId, String role, String userId, String createdBy) {
        TenantMembershipTable table = new TenantMembershipTable();
        table.setTenantId(tenantId);
        table.setUserId(userId);
        table.setRole(role);
        table.setCreatedAt(OffsetDateTime.now());
        table.setCreatedBy(createdBy);
        dynamoDbTable.putItem(table);
    }

    public List<TenantMembershipTable> getTenantsForUserId(String userId) {
        TenantMembershipTable keyItem = new TenantMembershipTable();
        keyItem.setUserId(userId);

        DynamoDbIndex<TenantMembershipTable> index = dynamoDbTable.index("user-lookup-index");

        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue(userId));
        SdkIterable<Page<TenantMembershipTable>> result = index.query(QueryEnhancedRequest.builder().queryConditional(queryConditional).build());

        return result.stream().map(Page::items).flatMap(Collection::stream).toList();
    }
}
