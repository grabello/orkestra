package com.orkestra.storage.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.storage.dynamodb.model.TenantMembershipTable;
import com.orkestra.storage.dynamodb.model.TenantTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantRepositoryTest {


    @Mock
    private DynamoDbIndex<TenantTable> dbIndex;

    @Mock
    SdkIterable<Page<TenantTable>> page;

    @Mock
    private DynamoDbTable<TenantTable> dynamoDbTable;

    @Mock
    private ObjectMapper objectMapper;

    private TenantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TenantRepository(dynamoDbTable, objectMapper);
    }

    @Test
    void getTenant() {
        TenantTable expected = new TenantTable();
        expected.setSlug("acme");
        expected.setName("Acme Co.");
        expected.setTenantId("tenant-123");
        expected.setCreatedBy("someUser");

        when(dynamoDbTable.getItem(any(TenantTable.class))).thenReturn(expected);

        TenantTable result = repository.getTenant("tenant-123");

        assertNotNull(result);
        assertEquals(expected, result);

        ArgumentCaptor<TenantTable> captor = ArgumentCaptor.forClass(TenantTable.class);
        verify(dynamoDbTable).getItem(captor.capture());

        TenantTable keyItem = captor.getValue();
        assertEquals("tenant-123", keyItem.getTenantId());
    }

    @Test
    void save() {

        repository.save("tenant-123", "acme", "Acme Co.", "someUser");

        ArgumentCaptor<TenantTable> captor = ArgumentCaptor.forClass(TenantTable.class);
        verify(dynamoDbTable).putItem(captor.capture());
        TenantTable keyItem = captor.getValue();
        assertEquals("acme", keyItem.getSlug());
        assertEquals("tenant-123", keyItem.getTenantId());
        assertEquals("Acme Co.", keyItem.getName());
        assertEquals("someUser", keyItem.getCreatedBy());
    }

    @Test
    void getTenantFromSlug() {
        String userId = "user-123";

        TenantTable tenantTable = new TenantTable();
        tenantTable.setName("Acme Co.");
        tenantTable.setSlug("acme");
        tenantTable.setTenantId("tenant-123");
        tenantTable.setCreatedBy("someUser");

        @SuppressWarnings("unchecked")
        Page<TenantTable> page1 = mock(Page.class);
        when(page1.items()).thenReturn(List.of(tenantTable));


        @SuppressWarnings("unchecked")
        SdkIterable<Page<TenantTable>> iterable = mock(SdkIterable.class);

        when(dynamoDbTable.index("slug-index")).thenReturn(dbIndex);
        when(dbIndex.query(any(QueryEnhancedRequest.class))).thenReturn(iterable);
        when(iterable.stream()).thenReturn(List.of(page1).stream());

        TenantTable result = repository.getTenantFromSlug("acme");

        assertNotNull(result);
        assertEquals("tenant-123", result.getTenantId());
        assertEquals("acme", result.getSlug());
        assertEquals("Acme Co.", result.getName());
        assertEquals("someUser", result.getCreatedBy());

        verify(dynamoDbTable).index("slug-index");

        ArgumentCaptor<QueryEnhancedRequest> captor =
                ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(dbIndex).query(captor.capture());

        QueryEnhancedRequest captured = captor.getValue();
        assertNotNull(captured);
        assertNotNull(captured.queryConditional());
    }

    @Test
    void getAllTenants_shouldReturnAllItemsFromScan() {
        TenantTable tenant1 = new TenantTable();
        tenant1.setTenantId("tenant-1");
        tenant1.setName("Acme");

        TenantTable tenant2 = new TenantTable();
        tenant2.setTenantId("tenant-2");
        tenant2.setName("Globex");

        @SuppressWarnings("unchecked")
        PageIterable<TenantTable> pageIterable = mock(PageIterable.class);

        @SuppressWarnings("unchecked")
        SdkIterable<TenantTable> itemsIterable = mock(SdkIterable.class);

        when(dynamoDbTable.scan()).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(itemsIterable);
        when(itemsIterable.stream()).thenReturn(List.of(tenant1, tenant2).stream());

        List<TenantTable> result = repository.getAllTenants();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("tenant-1", result.get(0).getTenantId());
        assertEquals("tenant-2", result.get(1).getTenantId());

        verify(dynamoDbTable).scan();
        verify(pageIterable).items();
    }

}
