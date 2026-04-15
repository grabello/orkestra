package com.orkestra.app.web;

import com.orkestra.api.model.CurrentUserResponse;
import com.orkestra.app.security.CurrentUserProvider;
import com.orkestra.app.web.generated.AuthApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final CurrentUserProvider currentUserProvider;

    @Override
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        CurrentUserResponse response = new CurrentUserResponse();
        response.setEmail(currentUserProvider.email());
        response.setSubject(currentUserProvider.userId());
        response.setIssuer(currentUserProvider.issuer());
        return ResponseEntity.ok(response);
    }
}
