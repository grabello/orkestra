package com.orkestra.app.web;

import com.orkestra.api.model.CreateTenantMembershipRequest;
import com.orkestra.api.model.CreateTenantRequest;
import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.api.model.TenantMembershipResponse;
import com.orkestra.api.model.TenantResponse;
import com.orkestra.app.security.CurrentUserProvider;
import com.orkestra.app.security.TenantFilter;
import com.orkestra.app.service.AdminService;
import com.orkestra.app.service.MembershipService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private AdminService adminService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return new tenant")
    void shouldReturnNewTenant() throws Exception {
        when(currentUserProvider.userId()).thenReturn("user-123");
        TenantResponse value = new TenantResponse();
        value.setTenantId("tenant-123");
        value.setSlug("acme");
        value.setName("Acme Corp");
        CreateTenantRequest createTenantRequest = new CreateTenantRequest();
        createTenantRequest.setSlug("acme");
        createTenantRequest.setName("Acme Corp");
        createTenantRequest.setCreateCreatorMembership(true);
        when(adminService.createTenant("user-123", createTenantRequest)).thenReturn(value);

        mockMvc.perform(post("/admin/v1/tenants").contentType(APPLICATION_JSON).content("""
                                                                                                {
                                                                                                  "slug": "acme",
                                                                                                  "name": "Acme Corp",
                                                                                                  "createCreatorMembership": true
                                                                                                }
                                                                                                """))
               .andExpect(status().isCreated())
               .andDo(print())
               .andExpect(jsonPath("$.tenantId").value("tenant-123"))
               .andExpect(jsonPath("$.slug").value("acme"))
               .andExpect(jsonPath("$.name").value("Acme Corp"));
    }

    @Test
    @DisplayName("Should return all tenants")
    void shouldReturnAllTenants() throws Exception {
        List<TenantResponse> tenants = List.of(new TenantResponse().tenantId("tenant-123").slug("acme").name("Acme Corp"));
        ListTenantsResponse response = new ListTenantsResponse().items(tenants);
        when(adminService.getAllTenants()).thenReturn(response);

        mockMvc.perform(get("/admin/v1/tenants").contentType(APPLICATION_JSON))
               .andExpect(status().isOk())
               .andDo(print())
               .andExpect(jsonPath("$..tenantId").value("tenant-123"))
               .andExpect(jsonPath("$..slug").value("acme"))
               .andExpect(jsonPath("$..name").value("Acme Corp"));
    }

    @Test
    @DisplayName("Should create membership and return information")
    void shouldReturnMembershipInformation() throws Exception {
        when(currentUserProvider.userId()).thenReturn("user-123");

        CreateTenantMembershipRequest createTenantMembershipRequest = new CreateTenantMembershipRequest();
        createTenantMembershipRequest.setUserId("user-456");
        createTenantMembershipRequest.setRole(CreateTenantMembershipRequest.RoleEnum.ADMIN);

        TenantMembershipResponse response = new TenantMembershipResponse();
        response.setCreatedAt(OffsetDateTime.now());
        response.setRole(TenantMembershipResponse.RoleEnum.ADMIN);
        response.setCreatedBy("user-123");
        response.setTenantId("tenant-123");
        response.setUserId("user-456");
        when(adminService.createTenantMembership("user-123", "tenant-123", createTenantMembershipRequest)).thenReturn(response);

        mockMvc.perform(post("/admin/v1/tenants/tenant-123/memberships").contentType(APPLICATION_JSON)
                                .content("""
                                                 {
                                                   "userId": "user-456",
                                                   "role": "ADMIN"
                                                 }

                                                 """)
               )
               .andExpect(status().isCreated())
               .andDo(print())
               .andExpect(jsonPath("$.tenantId").value("tenant-123"))
               .andExpect(jsonPath("$.userId").value("user-456"))
               .andExpect(jsonPath("$.createdBy").value("user-123"));
    }
}
