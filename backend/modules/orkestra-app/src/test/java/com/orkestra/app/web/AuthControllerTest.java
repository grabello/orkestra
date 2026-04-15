package com.orkestra.app.web;

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

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private TenantFilter tenantFilter;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @DisplayName("Should return current authenticated user")
    void shouldReturnCurrentUser() throws Exception {
        when(currentUserProvider.email()).thenReturn("gabriel@example.com");
        when(currentUserProvider.userId()).thenReturn("user-123");
        when(currentUserProvider.issuer()).thenReturn("https://example.supabase.co/auth/v1");

        mockMvc.perform(get("/v1/me")
                                .contentType(APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
               .andExpect(jsonPath("$.email").value("gabriel@example.com"))
               .andExpect(jsonPath("$.subject").value("user-123"))
               .andExpect(jsonPath("$.issuer").value("https://example.supabase.co/auth/v1"));
    }
}
