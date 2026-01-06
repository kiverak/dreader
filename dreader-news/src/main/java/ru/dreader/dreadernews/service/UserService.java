package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import dto.UserInfo;

@Service
@RequiredArgsConstructor
public class UserService {

    private final WebClient.Builder webClientBuilder;

    public UserInfo getCurrentUserInfo() {
        return webClientBuilder.build()
                .get()
                .uri("http://dreader-users/api/user")
                .retrieve()
                .bodyToMono(UserInfo.class)
                .block();
    }
}
