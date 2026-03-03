package ru.dreader.dreadernews.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreadernews.dto.ThreadsShortLivedTokenRequest;
import ru.dreader.dreadernews.service.ThreadsTokenService;

@RestController
@RequestMapping("/api/admin/threads")
@RequiredArgsConstructor
public class AdminThreadsController {

    private final ThreadsTokenService tokenService;

    // TODO remake with only a code
    @PostMapping()
    public void create(@RequestBody ThreadsShortLivedTokenRequest request) {
        tokenService.saveShortLivedToken(request);
    }

}
