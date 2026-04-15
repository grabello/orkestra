package com.orkestra.app.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserProviderTest {

    private final CurrentUserProvider currentUserProvider = new CurrentUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void email_shouldReturnEmailClaim() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "user-123",
                "email", "gabriel@example.com",
                "iss", "https://example.supabase.co/auth/v1"
        ));

        SecurityContextHolder.getContext()
                             .setAuthentication(new TestingAuthenticationToken(jwt, null));

        assertEquals("gabriel@example.com", currentUserProvider.email());
    }

    @Test
    void userId_shouldReturnSubClaim() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "user-123",
                "email", "gabriel@example.com",
                "iss", "https://example.supabase.co/auth/v1"
        ));

        SecurityContextHolder.getContext()
                             .setAuthentication(new TestingAuthenticationToken(jwt, null));

        assertEquals("user-123", currentUserProvider.userId());
    }

    @Test
    void userId_shouldReturnNull() {
        Jwt jwt = buildJwt(Map.of(
                "email", "gabriel@example.com",
                "iss", "https://example.supabase.co/auth/v1"
        ));

        SecurityContextHolder.getContext()
                             .setAuthentication(new TestingAuthenticationToken(jwt, null));

        assertNull(currentUserProvider.userId());
    }

    @Test
    void issuer_shouldReturnIssClaim() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "user-123",
                "email", "gabriel@example.com",
                "iss", "https://example.supabase.co/auth/v1"
        ));

        SecurityContextHolder.getContext()
                             .setAuthentication(new TestingAuthenticationToken(jwt, null));

        assertEquals("https://example.supabase.co/auth/v1", currentUserProvider.issuer());
    }

    @Test
    void jwt_shouldReturnJwtFromSecurityContext() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "user-123",
                "email", "gabriel@example.com",
                "iss", "https://example.supabase.co/auth/v1"
        ));

        SecurityContextHolder.getContext()
                             .setAuthentication(new TestingAuthenticationToken(jwt, null));

        assertSame(jwt, currentUserProvider.jwt());
    }

    @Test
    void shouldThrowWhenNoAuthenticationExists() {
        SecurityContextHolder.clearContext();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> currentUserProvider.userId()
        );

        assertEquals("No authenticated JWT found in security context", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPrincipalIsNotJwt() {
        SecurityContextHolder.getContext()
                             .setAuthentication(new TestingAuthenticationToken("not-a-jwt", null));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> currentUserProvider.jwt()
        );

        assertEquals("No authenticated JWT found in security context", ex.getMessage());
    }

    @Test
    void shouldReturnNullWhenClaimIsMissing() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "user-123"
        ));

        SecurityContextHolder.getContext()
                             .setAuthentication(new TestingAuthenticationToken(jwt, null));

        assertNull(currentUserProvider.email());
        assertNull(currentUserProvider.issuer());
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims
        );
    }
}
