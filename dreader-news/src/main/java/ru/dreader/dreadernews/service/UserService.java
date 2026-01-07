package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import dto.UserInfo;

@Service
@RequiredArgsConstructor
public class UserService {

    private final WebClient userWebClient;
    private final WebClient serviceWebClient;

    // user request
    public UserInfo getCurrentUserInfo() {
        return userWebClient
                .get()
                .uri("localhost:8765/users/api/user")
                .retrieve()
                .bodyToMono(UserInfo.class)
                .block();
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
