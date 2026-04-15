package com.orkestra.app.service;

import com.orkestra.api.model.CreateTenantMembershipRequest;
import com.orkestra.api.model.CreateTenantRequest;
import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.api.model.TenantMembershipResponse;
import com.orkestra.api.model.TenantResponse;
import com.orkestra.app.converter.ModelConverter;
import com.orkestra.app.cursor.CursorCodec;
import com.orkestra.app.exception.TenantNotFoundException;
import com.orkestra.storage.dynamodb.PlatformAdminRepository;
import com.orkestra.storage.dynamodb.TenantRepository;
import com.orkestra.storage.dynamodb.model.TenantTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private MembershipService membershipService;

    @Mock
    private PlatformAdminRepository platformAdminRepository;

    @Mock
    private CursorCodec cursorCodec;

    private ModelConverter modelConverter;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        modelConverter = new ModelConverter(cursorCodec);
        adminService = new AdminService(
                tenantRepository,
                membershipService,
                platformAdminRepository,
                modelConverter
        );
    }

    @Test
    void createTenant_slugAlreadyExists_throwException() {
        CreateTenantRequest request = getCreateTenantRequest(true);
        when(tenantRepository.getTenantFromSlug(request.getSlug())).thenReturn(new TenantTable());
        assertThrows(IllegalArgumentException.class, () -> adminService.createTenant("user-123", request));
    }

    @Test
    void createTenant_createMembership_saveTenantAndMembership() {

        CreateTenantRequest request = getCreateTenantRequest(true);
        when(tenantRepository.getTenantFromSlug(request.getSlug())).thenReturn(null);

        TenantTable tenantTable = new TenantTable();
        tenantTable.setTenantId("tenant-123");
        tenantTable.setSlug("slug");
        tenantTable.setName("name");
        when(tenantRepository.getTenant(anyString())).thenReturn(tenantTable);

        TenantResponse tenant = adminService.createTenant("user-123", request);

        assertNotNull(tenant);
        assertEquals("tenant-123", tenant.getTenantId());
        assertEquals("slug", tenant.getSlug());
        assertEquals("name", tenant.getName());

        verify(tenantRepository, times(1)).save(anyString(), eq("slug"), eq("name"), eq("user-123"));
        verify(membershipService, times(1)).addMember(anyString(), eq("user-123"), eq("ADMIN"), eq("user-123"));
    }

    @Test
    void createTenant_noMembership_saveTenantOnly() {

        CreateTenantRequest request = getCreateTenantRequest(false);
        when(tenantRepository.getTenantFromSlug(request.getSlug())).thenReturn(null);

        TenantTable tenantTable = new TenantTable();
        tenantTable.setTenantId("tenant-123");
        tenantTable.setSlug("slug");
        tenantTable.setName("name");
        when(tenantRepository.getTenant(anyString())).thenReturn(tenantTable);

        TenantResponse tenant = adminService.createTenant("user-123", request);

        assertNotNull(tenant);
        assertEquals("tenant-123", tenant.getTenantId());
        assertEquals("slug", tenant.getSlug());
        assertEquals("name", tenant.getName());

        verify(tenantRepository, times(1)).save(anyString(), eq("slug"), eq("name"), eq("user-123"));
        verify(membershipService, never()).addMember(anyString(), eq("user-123"), eq("ADMIN"), eq("user-123"));
    }

    @Test
    void isPlatformAdmin() {
        when(platformAdminRepository.isPlatformAdmin("user-123")).thenReturn(true);

        assertTrue(adminService.isAdmin("user-123"));
    }

    @Test
    void getAllTenants_repositoryReturnsNull_returnEmptyList() {
        when(tenantRepository.getAllTenants()).thenReturn(null);

        ListTenantsResponse allTenants = adminService.getAllTenants();
        assertEquals(new ListTenantsResponse(), allTenants);
    }

    @Test
    void getAllTenants_repositoryReturnsEmpty_returnEmptyList() {
        when(tenantRepository.getAllTenants()).thenReturn(List.of());

        ListTenantsResponse allTenants = adminService.getAllTenants();
        assertEquals(new ListTenantsResponse(), allTenants);
    }

    @Test
    void getAllTenants_happyCase() {
        TenantTable tenant = new TenantTable();
        tenant.setTenantId("tenant-123");
        tenant.setSlug("slug");
        tenant.setName("name");
        List<TenantTable> tenantTableList = List.of(tenant);
        when(tenantRepository.getAllTenants()).thenReturn(tenantTableList);

        ListTenantsResponse allTenants = adminService.getAllTenants();
        ListTenantsResponse expected = new ListTenantsResponse();
        TenantResponse tenantResponse = new TenantResponse();
        tenantResponse.setTenantId("tenant-123");
        tenantResponse.setSlug("slug");
        tenantResponse.setName("name");
        expected.setItems(List.of(tenantResponse));
        assertEquals(expected, allTenants);
    }

    @Test
    void createTenantMembership_tenantExists_returnMembership() {
        when(tenantRepository.getTenant("tenant-123")).thenReturn(new TenantTable());

        CreateTenantMembershipRequest createTenantMembershipRequest = new CreateTenantMembershipRequest();
        createTenantMembershipRequest.setUserId("user-123");
        createTenantMembershipRequest.setRole(CreateTenantMembershipRequest.RoleEnum.ADMIN);
        when(adminService.createTenantMembership("user-123", "tenant-123", createTenantMembershipRequest)).thenReturn(new TenantMembershipResponse());

        TenantMembershipResponse result = adminService.createTenantMembership("user-123", "tenant-123", createTenantMembershipRequest);
        assertEquals(new TenantMembershipResponse(), result);
    }

    @Test
    void createTenantMembership_tenantDoesNotExists_throwException() {
        when(tenantRepository.getTenant("tenant-123")).thenReturn(null);

        assertThrows(TenantNotFoundException.class, () -> adminService.createTenantMembership("user-123", "tenant-123", new CreateTenantMembershipRequest()));
    }

    private static CreateTenantRequest getCreateTenantRequest(boolean createCreatorMembership) {
        CreateTenantRequest request = new CreateTenantRequest();
        request.setSlug("slug");
        request.setName("name");
        request.setCreateCreatorMembership(createCreatorMembership);
        return request;
    }
}
