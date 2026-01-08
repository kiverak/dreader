package ru.dreader.dreaderusers.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
/*
кастомная модель пользователя в SecurityContext, если нужно хранить не только роли, но и permissions
 */
@Getter
public class DreaderOidcUser extends DefaultOidcUser {

    private final Set<String> allPermissions = new HashSet<>();

    public DreaderOidcUser(OidcIdToken idToken, List<GrantedAuthority> authorities, Set<String> permissions) {
        super(authorities, idToken);
        allPermissions.addAll(permissions);
    }

    public String getEmail() {
        return getIdToken().getEmail();
    }

}
