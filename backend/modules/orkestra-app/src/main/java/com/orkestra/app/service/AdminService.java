package com.orkestra.app.service;

import com.orkestra.api.model.CreateTenantMembershipRequest;
import com.orkestra.api.model.CreateTenantRequest;
import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.api.model.TenantMembershipResponse;
import com.orkestra.api.model.TenantResponse;
import com.orkestra.app.converter.ModelConverter;
import com.orkestra.app.exception.TenantNotFoundException;
import com.orkestra.storage.dynamodb.PlatformAdminRepository;
import com.orkestra.storage.dynamodb.TenantRepository;
import com.orkestra.storage.dynamodb.model.TenantTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final TenantRepository tenantRepository;
    private final MembershipService membershipService;
    private final PlatformAdminRepository platformAdminRepository;
    private final ModelConverter modelConverter;

    public TenantResponse createTenant(String userId, CreateTenantRequest createTenantRequest) {
        if (tenantRepository.getTenantFromSlug(createTenantRequest.getSlug()) != null) {
            throw new IllegalArgumentException("Tenant already exists");
        }

        String tenantId = "tenant-" + UUID.randomUUID().toString().replace("-", "");

        tenantRepository.save(tenantId, createTenantRequest.getSlug(), createTenantRequest.getName(), userId);

        if (createTenantRequest.getCreateCreatorMembership()) {
            membershipService.addMember(tenantId, userId, "ADMIN", userId);
        }

        TenantTable tenant = tenantRepository.getTenant(tenantId);

        TenantResponse tenantResponse = new TenantResponse();
        tenantResponse.setTenantId(tenant.getTenantId());
        tenantResponse.setSlug(tenant.getSlug());
        tenantResponse.setName(tenant.getName());
        tenantResponse.setCreatedAt(tenant.getCreatedAt());
        tenantResponse.setCreatedBy(tenant.getCreatedBy());

        return tenantResponse;
    }

    public boolean isAdmin(String userId) {
        return platformAdminRepository.isPlatformAdmin(userId);
    }

    public ListTenantsResponse getAllTenants() {
        List<TenantTable> allTenants = tenantRepository.getAllTenants();
        ListTenantsResponse response = new ListTenantsResponse();
        if (allTenants == null || allTenants.isEmpty()) {
            response.setItems(List.of());
            return response;
        }

        List<TenantResponse> list = allTenants.stream().map(modelConverter::fromTableResult).toList();
        response.setItems(list);
        return response;
    }

    public TenantMembershipResponse createTenantMembership(String userId, String tenantId, CreateTenantMembershipRequest createTenantMembershipRequest) {
        TenantTable tenant = tenantRepository.getTenant(tenantId);
        if (tenant == null) {
            throw new TenantNotFoundException("TENANT_NOT_FOUND", "Tenant does not exist");
        }
        return membershipService.addMember(tenantId, createTenantMembershipRequest.getUserId(), createTenantMembershipRequest.getRole().getValue(), userId);
    }
}
