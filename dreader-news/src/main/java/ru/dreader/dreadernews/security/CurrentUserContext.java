package ru.dreader.dreadernews.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CurrentUserContext {

    private Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getUserId() {
        return getJwt().getSubject(); // Keycloak userId
    }

    public String getEmail() {
        return getJwt().getClaim("email");
    }

    public String getUsername() {
        return getJwt().getClaim("preferred_username");
    }

    public List<String> getRoles() {
        return (List<String>) getJwt().getClaimAsMap("realm_access").get("roles");
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }
}
