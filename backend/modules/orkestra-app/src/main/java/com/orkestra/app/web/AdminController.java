package com.orkestra.app.web;

import com.orkestra.api.model.CreateTenantMembershipRequest;
import com.orkestra.api.model.CreateTenantRequest;
import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.api.model.TenantMembershipResponse;
import com.orkestra.api.model.TenantResponse;
import com.orkestra.app.security.CurrentUserProvider;
import com.orkestra.app.service.AdminService;
import com.orkestra.app.web.generated.AdminApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final AdminService adminService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ResponseEntity<TenantResponse> createTenant(CreateTenantRequest createTenantRequest) {
        String userId = currentUserProvider.userId();
        TenantResponse response = adminService.createTenant(userId, createTenantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<TenantMembershipResponse> createTenantMembership(String tenantId, CreateTenantMembershipRequest createTenantMembershipRequest) {
        String userId = currentUserProvider.userId();
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createTenantMembership(userId, tenantId, createTenantMembershipRequest));
    }

    @Override
    public ResponseEntity<ListTenantsResponse> listTenantsAdmin() {
        return ResponseEntity.ok(adminService.getAllTenants());
    }
}
