package ru.dreader.dreaderusers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.dto.UserInfo;
import ru.dreader.dreaderusers.service.UserService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping()
    public UserInfo getCurrentUserInfo() {
        return userService.getCurrentUserInfo();
    }

    @PutMapping()
    public UserInfo updateUser(@RequestBody UserDto userDto) {
        String keycloakId = userService.getCurrentUserInfo().getId();
        return userService.updateUser(keycloakId, userDto);
    }

    @DeleteMapping()
    public void deleteUser() {
        String keycloakId = userService.getCurrentUserInfo().getId();
        userService.deleteUser(keycloakId);
    }

}
