package ru.dreader.dreaderllmparser.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dreader.dreaderllmparser.dto.ArticleAiAnalysis;
import ru.dreader.dreaderllmparser.dto.ArticleResponse;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleProcessingServiceTest {

    @Mock
    private ArticleAiService articleAiService;

    @InjectMocks
    private ArticleProcessingService articleProcessingService;

    @Test
    void processHtml_CallsAiServiceAndReturnsResponse() {
        String url = "http://example.com";
        String title = "Title";
        String body = "Body";
        List<String> tags = List.of("tag");
        Locale locale = Locale.ENGLISH;

        ArticleAiAnalysis analysis = new ArticleAiAnalysis(
                "Summary",
                List.of("Point"),
                "Main",
                List.of("Secondary")
        );

        when(articleAiService.analyze(title, body, tags, locale)).thenReturn(analysis);

        ArticleResponse response = articleProcessingService.processHtml(url, title, body, tags, locale);

        assertEquals(url, response.url());
        assertEquals(title, response.title());
        assertEquals("Summary", response.summary());
        assertEquals(List.of("Point"), response.summaryBullets());
        assertEquals("Main", response.mainCategory());
        assertEquals(List.of("Secondary"), response.secondaryCategories());
    }
}
