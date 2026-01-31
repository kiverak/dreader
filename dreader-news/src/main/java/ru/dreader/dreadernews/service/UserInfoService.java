package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import dto.UserInfo;
import ru.dreader.dreadernews.security.CurrentUserContext;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    @Value("${urls.gateway}")
    private String gatewayUrl;

    private final CurrentUserContext currentUserContext;
    private final WebClient serviceWebClient;

    // from context
    public UserInfo getCurrentUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(currentUserContext.getUserId());
        userInfo.setUsername(currentUserContext.getUsername());
        userInfo.setEmail(currentUserContext.getEmail());

        return userInfo;
    }

    // service request
    public UserInfo getUserById(String id) {
        return serviceWebClient
                .get()
                .uri(gatewayUrl + "/users/api/internal/users/" + id)
                .retrieve()
                .bodyToMono(UserInfo.class)
                .block();
    }
}
