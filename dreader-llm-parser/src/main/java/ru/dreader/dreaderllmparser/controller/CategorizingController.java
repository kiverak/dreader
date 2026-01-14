package ru.dreader.dreaderllmparser.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreader.dreaderllmparser.dto.CategorizingRequest;
import ru.dreader.dreaderllmparser.dto.CategorizingResponse;
import ru.dreader.dreaderllmparser.service.CategorizingService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategorizingController {

    private final CategorizingService categorizingService;

    @PostMapping("/categorize")
    public CategorizingResponse categorize(@RequestBody CategorizingRequest request) {
        return categorizingService.rankArticles(request);
    }
}
