package com.orkestra.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public String email() {
        Jwt jwt = currentJwt();
        Object email = jwt.getClaims().get("email");
        return email != null ? email.toString() : null;
    }

    public String userId() {
        Jwt jwt = currentJwt();
        Object id = jwt.getClaims().get("sub");
        return id != null ? id.toString() : null;
    }

    public String issuer() {
        Jwt jwt = currentJwt();
        Object issuer = jwt.getClaims().get("iss");
        return issuer != null ? issuer.toString() : null;
    }

    public Jwt jwt() {
        return currentJwt();
    }

    private Jwt currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No authenticated JWT found in security context");
        }
        return jwt;
    }
}
