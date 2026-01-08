package ru.dreader.dreadernews.controller;

import dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreader.dreadernews.service.UserInfoService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserInfoService userInfoService;

    @GetMapping("/me")
    public UserInfo getMe() {
        return userInfoService.getCurrentUserInfo();
    }

}
