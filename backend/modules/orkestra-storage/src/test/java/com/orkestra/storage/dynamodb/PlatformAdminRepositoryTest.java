package com.orkestra.storage.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.storage.dynamodb.model.PlatformAdminTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformAdminRepositoryTest {

    @Mock
    private DynamoDbTable<PlatformAdminTable> dynamoDbTable;

    @Mock
    private ObjectMapper objectMapper;

    private PlatformAdminRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PlatformAdminRepository(dynamoDbTable, objectMapper);
    }

    @Test
    void getPlatformAdminUser_returnUser() {
        PlatformAdminTable expected = new PlatformAdminTable();
        expected.setUserId("user-123");
        expected.setEmail("some@example.com");
        expected.setStatus("ACTIVE");
        expected.setCreatedBy("someUser");

        when(dynamoDbTable.getItem(any(PlatformAdminTable.class))).thenReturn(expected);

        PlatformAdminTable result = repository.getPlatformAdminUser("user-123");

        assertNotNull(result);
        assertEquals(expected, result);

        ArgumentCaptor<PlatformAdminTable> captor = ArgumentCaptor.forClass(PlatformAdminTable.class);
        verify(dynamoDbTable).getItem(captor.capture());

        PlatformAdminTable keyItem = captor.getValue();
        assertEquals("user-123", keyItem.getUserId());
    }

    @Test
    void isAdmin_isAdmin_returnTrue() {
        PlatformAdminTable expected = new PlatformAdminTable();
        expected.setUserId("user-123");
        expected.setEmail("some@example.com");
        expected.setStatus("ACTIVE");
        expected.setCreatedBy("someUser");

        when(dynamoDbTable.getItem(any(PlatformAdminTable.class))).thenReturn(expected);

        assertTrue(repository.isPlatformAdmin("user-123"));

        ArgumentCaptor<PlatformAdminTable> captor = ArgumentCaptor.forClass(PlatformAdminTable.class);
        verify(dynamoDbTable).getItem(captor.capture());

        PlatformAdminTable keyItem = captor.getValue();
        assertEquals("user-123", keyItem.getUserId());
    }

    @Test
    void isAdmin_notAdmin_returnFalse() {
        when(dynamoDbTable.getItem(any(PlatformAdminTable.class))).thenReturn(null);

        assertFalse(repository.isPlatformAdmin("user-123"));


        ArgumentCaptor<PlatformAdminTable> captor = ArgumentCaptor.forClass(PlatformAdminTable.class);
        verify(dynamoDbTable).getItem(captor.capture());

        PlatformAdminTable keyItem = captor.getValue();
        assertEquals("user-123", keyItem.getUserId());
    }


}
