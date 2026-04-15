package com.orkestra.app.service;

import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.api.model.TenantMembershipResponse;
import com.orkestra.api.model.TenantResponse;
import com.orkestra.app.converter.ModelConverter;
import com.orkestra.storage.dynamodb.TenantMembershipRepository;
import com.orkestra.storage.dynamodb.TenantRepository;
import com.orkestra.storage.dynamodb.model.TenantMembershipTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final TenantMembershipRepository tenantMembershipRepository;
    private final ModelConverter modelConverter;
    private final TenantRepository tenantRepository;

    public boolean verifyUserTenantAccess(String userId, String tenantId) {
        return tenantMembershipRepository.getTenantMembership(tenantId, userId) != null;
    }

    public TenantMembershipResponse addMember(String tenantId, String userId, String role, String createdBy) {
        tenantMembershipRepository.save(tenantId, role, userId, createdBy);
        TenantMembershipTable tenantMembership = tenantMembershipRepository.getTenantMembership(tenantId, userId);
        return modelConverter.fromTableResult(tenantMembership);
    }

    public ListTenantsResponse listTenantsForUser(String userId) {
        List<TenantMembershipTable> membershipList = tenantMembershipRepository.getTenantsForUserId(userId);
        if (membershipList == null || membershipList.isEmpty()) {
            return new ListTenantsResponse();
        }
        List<TenantResponse> list = membershipList.stream().map(membership -> tenantRepository.getTenant(membership.getTenantId())).map(modelConverter::fromTableResult).toList();

        ListTenantsResponse listTenantsResponse = new ListTenantsResponse();
        listTenantsResponse.setItems(list);
        return listTenantsResponse;
    }
}
