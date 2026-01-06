package ru.dreader.dreaderusers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.dto.UserInfo;
import ru.dreader.dreaderusers.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping()
    public String login() {
        return "login planner-users";
    }

    @PostMapping("/register")
    public UserInfo createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

}
