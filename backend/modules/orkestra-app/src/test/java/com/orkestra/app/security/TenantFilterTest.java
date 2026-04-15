package com.orkestra.app.security;

import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.app.service.AdminService;
import com.orkestra.app.service.MembershipService;
import com.orkestra.app.service.WorkflowManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TenantFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private WorkflowManagementService workflowManagementService;

    @Test
    @DisplayName("Should allow public health endpoint without auth or tenant header")
    void shouldAllowPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andDo(print())
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject tenant-scoped request when X-Tenant-Id header is missing")
    void shouldRejectMissingTenantHeader() throws Exception {
        when(currentUserProvider.userId()).thenReturn("user-123");

        mockMvc.perform(get("/v1/workflows")
                                .with(jwt())
                                .contentType(APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.code").value("MISSING_TENANT"))
               .andExpect(jsonPath("$.messages[0]").value("Missing X-Tenant-Id header"));
    }

    @Test
    @DisplayName("Should reject tenant-scoped request when X-Tenant-Id header is blank")
    void shouldRejectBlankTenantHeader() throws Exception {
        when(currentUserProvider.userId()).thenReturn("user-123");

        mockMvc.perform(get("/v1/workflows")
                                .with(jwt())
                                .header("X-Tenant-Id", "")
                                .contentType(APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.code").value("MISSING_TENANT"))
               .andExpect(jsonPath("$.messages[0]").value("Missing X-Tenant-Id header"));
    }

    @Test
    @DisplayName("Should reject tenant-scoped request when user does not belong to tenant")
    void shouldRejectWhenUserHasNoTenantAccess() throws Exception {
        when(currentUserProvider.userId()).thenReturn("user-123");
        when(membershipService.verifyUserTenantAccess("user-123", "tenant-1")).thenReturn(false);

        mockMvc.perform(get("/v1/workflows")
                                .with(jwt())
                                .header("X-Tenant-Id", "tenant-1")
                                .contentType(APPLICATION_JSON))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.code").value("TENANT_FORBIDDEN"));
    }

    @Test
    @DisplayName("Should allow tenant-scoped request when user belongs to tenant")
    void shouldAllowWhenUserBelongsToTenant() throws Exception {
        when(currentUserProvider.userId()).thenReturn("user-123");
        when(membershipService.verifyUserTenantAccess("user-123", "tenant-1")).thenReturn(true);
        when(workflowManagementService.listWorkflows("tenant-1", null, null))
                .thenReturn(new com.orkestra.api.model.ListWorkflowsResponse().items(java.util.List.of()));

        mockMvc.perform(get("/v1/workflows")
                                .with(jwt())
                                .header("X-Tenant-Id", "tenant-1")
                                .contentType(APPLICATION_JSON))
               .andExpect(status().isOk());;
    }

    @Test
    @DisplayName("Should allow admin endpoint without X-Tenant-Id when user is platform admin")
    void shouldAllowAdminPathWithoutTenantHeaderForAdmin() throws Exception {
        when(currentUserProvider.userId()).thenReturn("user-123");
        when(adminService.isAdmin("user-123")).thenReturn(true);
        when(adminService.getAllTenants())
                .thenReturn(new ListTenantsResponse().items(java.util.List.of()));

        mockMvc.perform(get("/admin/v1/tenants")
                                .with(jwt())
                                .contentType(APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject admin endpoint when user is not platform admin")
    void shouldRejectAdminPathForNonAdmin() throws Exception {
        when(currentUserProvider.userId()).thenReturn("user-123");
        when(adminService.isAdmin("user-123")).thenReturn(false);

        mockMvc.perform(get("/admin/v1/tenants")
                                .with(jwt())
                                .contentType(APPLICATION_JSON))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.code").value("ADMIN_FORBIDDEN"));
    }
}
