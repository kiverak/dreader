package ru.dreader.dreaderusers.controller;

import dto.UserInfo;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.service.UserService;

@Log4j2
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/{email}")
    public UserInfo getUserInfoByEmail(@PathVariable @Email String email) {
        return userService.getUserInfoByEmail(email);
    }

    @GetMapping("/{keycloakId}")
    public UserInfo getUserInfoById(@PathVariable String keycloakId) {
        return userService.getUserInfoById(keycloakId);
    }

    @GetMapping()
    public Page<UserInfo> getUsers(@RequestParam(defaultValue = "20") Integer size,
                                   @RequestParam(defaultValue = "0") Integer page,
                                   @RequestParam(defaultValue = "email") String sort,
                                   @RequestParam(defaultValue = "asc") String order) {
        return userService.getUsers(size, page, sort, order);
    }

    @PutMapping("/{keycloakId}")
    public dto.UserInfo updateUser(@PathVariable String keycloakId, @RequestBody UserDto userDto) {
        return userService.updateUser(keycloakId, userDto);
    }

    @DeleteMapping("/{keycloakId}")
    public void deleteUser(@PathVariable String keycloakId) {
        userService.deleteUser(keycloakId);
    }

}