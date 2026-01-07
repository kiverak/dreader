package ru.dreader.dreaderusers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public dto.UserInfo createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

}
