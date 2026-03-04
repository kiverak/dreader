package ru.dreader.dreadernews.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreader.dreadernews.dto.ThreadsCodeShortLivedTokenRequest;
import ru.dreader.dreadernews.service.ThreadsTokenService;

@RestController
@RequestMapping("/api/admin/threads")
@RequiredArgsConstructor
public class AdminThreadsController {

    private final ThreadsTokenService tokenService;

    @PostMapping()
    public void createToken(@RequestBody ThreadsCodeShortLivedTokenRequest request) {
        tokenService.requestAndSaveLongLivedToken(request);
    }
}
