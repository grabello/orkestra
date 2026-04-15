package com.orkestra.app.service;

import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.api.model.TenantMembershipResponse;
import com.orkestra.api.model.TenantResponse;
import com.orkestra.app.converter.ModelConverter;
import com.orkestra.app.cursor.CursorCodec;
import com.orkestra.storage.dynamodb.TenantMembershipRepository;
import com.orkestra.storage.dynamodb.TenantRepository;
import com.orkestra.storage.dynamodb.model.TenantMembershipTable;
import com.orkestra.storage.dynamodb.model.TenantTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private TenantMembershipRepository tenantMembershipRepository;
    private ModelConverter modelConverter;

    @Mock
    private CursorCodec cursorCodec;

    @Mock
    private TenantRepository tenantRepository;

    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        modelConverter = new ModelConverter(cursorCodec);
        membershipService = new MembershipService(tenantMembershipRepository, modelConverter, tenantRepository);
    }

    @Test
    void verifyUserTenantAccess_returnTrue() {
        when(tenantMembershipRepository.getTenantMembership("tenant-123", "user-123")).thenReturn(new TenantMembershipTable());
        assertTrue(membershipService.verifyUserTenantAccess("user-123", "tenant-123"));
    }

    @Test
    void verifyUserTenantAccess_returnFalse() {
        when(tenantMembershipRepository.getTenantMembership("tenant-123", "user-123")).thenReturn(null);
        assertFalse(membershipService.verifyUserTenantAccess("user-123", "tenant-123"));
    }

    @Test
    void addMember() {
        TenantMembershipTable tenantMembershipTable = new TenantMembershipTable();
        tenantMembershipTable.setTenantId("tenant-123");
        tenantMembershipTable.setUserId("user-123");
        tenantMembershipTable.setRole("ADMIN");
        tenantMembershipTable.setCreatedBy("user-456");

        when(tenantMembershipRepository.getTenantMembership("tenant-123", "user-123")).thenReturn(tenantMembershipTable);
        TenantMembershipResponse response = membershipService.addMember("tenant-123", "user-123", "ADMIN", "user-456");
        assertNotNull(response);
        assertEquals("tenant-123", response.getTenantId());
        assertEquals("user-123", response.getUserId());
        assertEquals("ADMIN", response.getRole().getValue());
        assertEquals("user-456", response.getCreatedBy());

        verify(tenantMembershipRepository).save("tenant-123", "ADMIN", "user-123", "user-456");
    }

    @Test
    void listTenantsForUser_nullTenants_returnEmpty() {
        when(tenantMembershipRepository.getTenantsForUserId("user-123")).thenReturn(null);

        assertEquals(new ListTenantsResponse(), membershipService.listTenantsForUser("user-123"));
    }

    @Test
    void listTenantsForUser_emptyTenants_returnEmpty() {
        when(tenantMembershipRepository.getTenantsForUserId("user-123")).thenReturn(List.of());

        assertEquals(new ListTenantsResponse(), membershipService.listTenantsForUser("user-123"));
    }

    @Test
    void listTenantsForUser_happyCase() {
        TenantMembershipTable tenantMembershipTable = new TenantMembershipTable();
        tenantMembershipTable.setTenantId("tenant-123");
        tenantMembershipTable.setUserId("user-123");
        tenantMembershipTable.setRole("ADMIN");
        tenantMembershipTable.setCreatedBy("user-456");
        when(tenantMembershipRepository.getTenantsForUserId("user-123")).thenReturn(List.of(tenantMembershipTable));
        TenantTable tenantTable = new TenantTable();
        tenantTable.setTenantId("tenant-123");
        tenantTable.setSlug("slug");
        tenantTable.setName("name");
        when(tenantRepository.getTenant("tenant-123")).thenReturn(tenantTable);

        ListTenantsResponse expected = new ListTenantsResponse();
        TenantResponse tenantResponse = new TenantResponse();
        tenantResponse.setTenantId("tenant-123");
        tenantResponse.setSlug("slug");
        tenantResponse.setName("name");

        expected.setItems(List.of(tenantResponse));
        assertEquals(expected, membershipService.listTenantsForUser("user-123"));

        verify(tenantRepository, times(1)).getTenant(anyString());
    }
}
