package com.orkestra.storage.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.storage.dynamodb.model.TenantTable;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;


@AllArgsConstructor
public class TenantRepository {

    private final DynamoDbTable<TenantTable> dynamoDbTable;
    private final ObjectMapper objectMapper;

    public TenantTable getTenant(String tenantId) {
        TenantTable keyItem = new TenantTable();
        keyItem.setTenantId(tenantId);
        return dynamoDbTable.getItem(keyItem);
    }

    public TenantTable getTenantFromSlug(String slug) {
        TenantTable keyItem = new TenantTable();
        keyItem.setSlug(slug);

        DynamoDbIndex<TenantTable> index = dynamoDbTable.index("slug-index");

        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue(slug));
        SdkIterable<Page<TenantTable>> result = index.query(QueryEnhancedRequest.builder().queryConditional(queryConditional).build());
        return result.stream().map(Page::items).flatMap(Collection::stream).findFirst().orElse(null);
    }

    public void save(String tenantId, String slug, String name, String createdBy) {
        TenantTable table = new TenantTable();
        table.setTenantId(tenantId);
        table.setSlug(slug);
        table.setName(name);
        table.setCreatedAt(OffsetDateTime.now());
        table.setCreatedBy(createdBy);
        dynamoDbTable.putItem(table);
    }

    public List<TenantTable> getAllTenants() {
        PageIterable<TenantTable> scan = dynamoDbTable.scan();
        return scan.items().stream().toList();
    }
}
