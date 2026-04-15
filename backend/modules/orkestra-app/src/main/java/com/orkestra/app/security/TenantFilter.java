package com.orkestra.app.security;

import com.orkestra.app.exception.AdminForbiddenException;
import com.orkestra.app.exception.MissingTenantIdException;
import com.orkestra.app.exception.TenantAccessDeniedException;
import com.orkestra.app.service.AdminService;
import com.orkestra.app.service.MembershipService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final CurrentUserProvider currentUserProvider;
    private final MembershipService membershipService;
    private final AdminService adminService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();

            if (isPublicPath(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            String userId = currentUserProvider.userId();

            if (isAdminPath(path)) {
                verifyAdminAccess(userId);
                filterChain.doFilter(request, response);
                return;
            }

            if (isTenantScopedPath(path)) {
                String tenantId = request.getHeader(TENANT_HEADER);
                verifyTenantHeaderPresent(tenantId);
                verifyUserAccessAndTenant(userId, tenantId);
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/health") || path.startsWith("/actuator");
    }

    private boolean isAdminPath(String path) {
        return path.startsWith("/admin/v1/");
    }

    private boolean isTenantScopedPath(String path) {
        return path.startsWith("/v1/");
    }

    private void verifyTenantHeaderPresent(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new MissingTenantIdException("MISSING_TENANT", "Missing X-Tenant-Id header");
        }
    }

    private void verifyAdminAccess(String userId) {
        if (!adminService.isAdmin(userId)) {
            throw new AdminForbiddenException("ADMIN_FORBIDDEN", "User is not an admin");
        }
    }

    private void verifyUserAccessAndTenant(String userId, String tenantId) {
        if (!membershipService.verifyUserTenantAccess(userId, tenantId)) {
            throw new TenantAccessDeniedException("TENANT_FORBIDDEN", "User does not have access to tenant");
        }
    }
}
