package ru.dreader.dreaderllmparser.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreader.dreaderllmparser.dto.ArticleResponse;
import ru.dreader.dreaderllmparser.dto.ParseRequest;
import ru.dreader.dreaderllmparser.service.ArticleProcessingService;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArticleController {

    private final ArticleProcessingService processingService;

    @PostMapping("/parse")
    public ArticleResponse parse(@RequestBody ParseRequest request) {
        String language = "ru";
        String region = "RU";
        return processingService.processHtml(
                request.url(),
                request.title(),
                request.body(),
                request.rawTags(),
                new Locale.Builder()
                        .setLanguage(language)
                        .setRegion(region)
                        .build()
        );
    }
}
