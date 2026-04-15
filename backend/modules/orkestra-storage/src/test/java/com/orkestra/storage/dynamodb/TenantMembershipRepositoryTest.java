package com.orkestra.storage.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.storage.dynamodb.model.TenantMembershipTable;
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
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TenantMembershipRepositoryTest {

    @Mock
    private DynamoDbIndex<TenantMembershipTable> dbIndex;

    @Mock
    SdkIterable<Page<TenantMembershipTable>> page;

    @Mock
    private DynamoDbTable<TenantMembershipTable> dynamoDbTable;

    @Mock
    private ObjectMapper objectMapper;

    private TenantMembershipRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TenantMembershipRepository(dynamoDbTable, objectMapper);
    }

    @Test
    void getTenantMembership() {
        TenantMembershipTable expected = new TenantMembershipTable();
        expected.setUserId("user-123");
        expected.setRole("ADMIN");
        expected.setTenantId("tenant-123");
        expected.setCreatedBy("someUser");

        when(dynamoDbTable.getItem(any(TenantMembershipTable.class))).thenReturn(expected);

        TenantMembershipTable result = repository.getTenantMembership("tenant-123", "user-123");

        assertNotNull(result);
        assertEquals(expected, result);

        ArgumentCaptor<TenantMembershipTable> captor = ArgumentCaptor.forClass(TenantMembershipTable.class);
        verify(dynamoDbTable).getItem(captor.capture());

        TenantMembershipTable keyItem = captor.getValue();
        assertEquals("user-123", keyItem.getUserId());
        assertEquals("tenant-123", keyItem.getTenantId());
    }

    @Test
    void save() {

        repository.save("tenant-123", "ADMIN", "user-123", "someUser");

        ArgumentCaptor<TenantMembershipTable> captor = ArgumentCaptor.forClass(TenantMembershipTable.class);
        verify(dynamoDbTable).putItem(captor.capture());
        TenantMembershipTable keyItem = captor.getValue();
        assertEquals("user-123", keyItem.getUserId());
        assertEquals("tenant-123", keyItem.getTenantId());
        assertEquals("ADMIN", keyItem.getRole());
        assertEquals("someUser", keyItem.getCreatedBy());
    }

    @Test
    void getTenantsForUserId_shouldQueryIndexAndFlattenAllPages() {
        String userId = "user-123";

        TenantMembershipTable membership1 = new TenantMembershipTable();
        membership1.setUserId(userId);
        membership1.setTenantId("tenant-1");

        TenantMembershipTable membership2 = new TenantMembershipTable();
        membership2.setUserId(userId);
        membership2.setTenantId("tenant-2");

        @SuppressWarnings("unchecked")
        Page<TenantMembershipTable> page1 = mock(Page.class);
        when(page1.items()).thenReturn(List.of(membership1));

        @SuppressWarnings("unchecked")
        Page<TenantMembershipTable> page2 = mock(Page.class);
        when(page2.items()).thenReturn(List.of(membership2));

        @SuppressWarnings("unchecked")
        SdkIterable<Page<TenantMembershipTable>> iterable = mock(SdkIterable.class);

        when(dynamoDbTable.index("user-lookup-index")).thenReturn(dbIndex);
        when(dbIndex.query(any(QueryEnhancedRequest.class))).thenReturn(iterable);
        when(iterable.stream()).thenReturn(List.of(page1, page2).stream());

        List<TenantMembershipTable> result = repository.getTenantsForUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("tenant-1", result.get(0).getTenantId());
        assertEquals("tenant-2", result.get(1).getTenantId());

        verify(dynamoDbTable).index("user-lookup-index");

        ArgumentCaptor<QueryEnhancedRequest> captor =
                ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(dbIndex).query(captor.capture());

        QueryEnhancedRequest captured = captor.getValue();
        assertNotNull(captured);
        assertNotNull(captured.queryConditional());
    }
}
