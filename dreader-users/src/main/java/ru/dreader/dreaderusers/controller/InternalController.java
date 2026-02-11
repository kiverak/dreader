package ru.dreader.dreaderusers.controller;

import dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreaderusers.service.UserService;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;

    @GetMapping("/users/{keycloakId}")
    public UserInfo getUserInfoById(@PathVariable String keycloakId) {
        return userService.getUserInfoById(keycloakId);
    }

}
