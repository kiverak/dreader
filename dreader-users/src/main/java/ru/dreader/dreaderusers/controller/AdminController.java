package ru.dreader.dreaderusers.controller;

import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.dto.UserInfo;
import ru.dreader.dreaderusers.service.UserService;

@Log4j2
@RestController
@RequestMapping("/api/users/admin")
@RequiredArgsConstructor
public class AdminController {

    public static final int CONFLICT = 409;

    private final UserService userService;

    @GetMapping("/{email}")
    public UserInfo getUserInfoByEmail(@PathVariable @Email String email) {
        return userService.getUserInfoByEmail(email);
    }

    @GetMapping("/{keycloakId}")
    public UserInfo getUserInfoById(@PathVariable String keycloakId) {
        return userService.getUserInfoById(keycloakId);
    }

    @PutMapping("/{keycloakId}")
    public UserInfo updateUser(@PathVariable String keycloakId, @RequestBody UserDto userDto) {
        return userService.updateUser(keycloakId, userDto);
    }

    @DeleteMapping("/{keycloakId}")
    public void deleteUser(@PathVariable String keycloakId) {
        userService.deleteUser(keycloakId);
    }

}