package ru.dreader.dreaderusers.service;

import entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreaderusers.auth.DreaderOidcUser;
import ru.dreader.dreaderusers.dto.UserInfo;
import ru.dreader.dreaderusers.repo.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final OidcUserService oidcUserService = new OidcUserService();
    private final UserRepository userRepository;

    // get
    @Transactional(readOnly = true)
    public UserInfo getUserInfoByEmail(final String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        UserInfo userInfo = new UserInfo();

        return userInfo;
    }

    @Transactional(readOnly = true)
    public UserInfo getCurrentUserInfo() {
        OAuth2User oAuth2User = getCurrentOidcUser();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        UserInfo userInfo = new UserInfo();

        return userInfo;
    }

    public DreaderOidcUser getCurrentOidcUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userIsLogin(auth))
            return (DreaderOidcUser) auth.getPrincipal();
        throw new RuntimeException("There are no login user.");
    }

    private boolean userIsLogin(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
    }

    // create

    // update

    // delete


}
