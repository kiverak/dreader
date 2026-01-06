package ru.dreader.dreaderllmparser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dreader.dreaderllmparser.dto.ArticleResponse;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ArticleProcessingService {

    private final ArticleAiService articleAiService;

    public ArticleResponse processHtml(String url, String title, String rawBody, List<String> rawTags, Locale locale) {

        var ai = articleAiService.analyze(
                title,
                rawBody,
                rawTags,
                locale
        );

        return new ArticleResponse(
                url,
                title,
                ai.summary(),
                ai.bulletPoints(),
                ai.mainCategory(),
                ai.secondaryCategories()
        );
    }

}
