package com.orkestra.app.web;

import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.api.model.TenantResponse;
import com.orkestra.app.security.CurrentUserProvider;
import com.orkestra.app.security.TenantFilter;
import com.orkestra.app.service.MembershipService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TenantController.class)
@AutoConfigureMockMvc(addFilters = false)
class TenantControllerTest {

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return all tenants for user")
    void shouldReturnAllTenantsByUser() throws Exception {
        List<TenantResponse> tenants = List.of(new TenantResponse().tenantId("tenant-123").slug("acme").name("Acme Corp"));
        ListTenantsResponse response = new ListTenantsResponse().items(tenants);
        when(currentUserProvider.userId()).thenReturn("user-123");
        when(membershipService.listTenantsForUser("user-123")).thenReturn(response);

        mockMvc.perform(get("/v1/tenants").contentType(APPLICATION_JSON))
               .andExpect(status().isOk())
               .andDo(print())
               .andExpect(jsonPath("$..tenantId").value("tenant-123"))
               .andExpect(jsonPath("$..slug").value("acme"))
               .andExpect(jsonPath("$..name").value("Acme Corp"));
    }

}
