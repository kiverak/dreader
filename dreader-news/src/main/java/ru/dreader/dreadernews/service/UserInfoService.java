package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import dto.UserInfo;
import ru.dreader.dreadernews.security.CurrentUserContext;

@Service
@RequiredArgsConstructor
public class UserInfoService {

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
                .uri("http://localhost:8765/users/api/internal/users/" + id)
                .retrieve()
                .bodyToMono(UserInfo.class)
                .block();
    }
}
