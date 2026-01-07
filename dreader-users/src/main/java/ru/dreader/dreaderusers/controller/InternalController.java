package ru.dreader.dreaderusers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.service.UserService;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;

    @GetMapping("/users/{keycloakId}")
    public dto.UserInfo getUserInfoById(@PathVariable String keycloakId) {
        return userService.getUserInfoById(keycloakId);
    }

}
