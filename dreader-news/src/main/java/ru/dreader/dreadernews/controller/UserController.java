package ru.dreader.dreadernews.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreader.dreadernews.security.CurrentUserContext;

import java.util.Map;

@RestController
@RequestMapping("/api/news/user")
@RequiredArgsConstructor
public class UserController {

    private final CurrentUserContext currentUserContext;

    @GetMapping()
    public Map<String, Object> getCurrentUser() {
        return Map.of(
                "id", currentUserContext.getUserId(),
                "username", currentUserContext.getUsername(),
                "email", currentUserContext.getEmail(),
                "roles", currentUserContext.getRoles()
        );
    }
}
