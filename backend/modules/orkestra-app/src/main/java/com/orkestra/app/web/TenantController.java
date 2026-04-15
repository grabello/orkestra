package com.orkestra.app.web;

import com.orkestra.api.model.ListTenantsResponse;
import com.orkestra.app.security.CurrentUserProvider;
import com.orkestra.app.service.MembershipService;
import com.orkestra.app.web.generated.TenantApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TenantController implements TenantApi {

    private final CurrentUserProvider currentUserProvider;
    private final MembershipService membershipService;

    @Override
    public ResponseEntity<ListTenantsResponse> listMyTenants() {

        String userId = currentUserProvider.userId();
        ListTenantsResponse response = membershipService.listTenantsForUser(userId);
        return ResponseEntity.ok(response);
    }
}
