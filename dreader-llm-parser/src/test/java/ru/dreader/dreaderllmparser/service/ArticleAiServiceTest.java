package ru.dreader.dreaderllmparser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import ru.dreader.dreaderllmparser.dto.ArticleAiAnalysis;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleAiServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private ArticleAiService articleAiService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        articleAiService = new ArticleAiService(chatClientBuilder);
    }

    @Test
    void analyze_ValidResponse_ReturnsAnalysis() {
        String jsonResponse = """
                {
                  "summary": "Test summary",
                  "bulletPoints": ["Point 1", "Point 2"],
                  "mainCategory": "Tech",
                  "secondaryCategories": ["AI"]
                }
                """;

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(jsonResponse);

        ArticleAiAnalysis result = articleAiService.analyze("Title", "Body", List.of("tag"), Locale.ENGLISH);

        assertEquals("Test summary", result.summary());
        assertEquals(2, result.bulletPoints().size());
        assertEquals("Tech", result.mainCategory());
        assertEquals(1, result.secondaryCategories().size());
    }

    @Test
    void analyze_InvalidJson_ReturnsEmptyAnalysis() {
        String invalidResponse = "Not JSON";

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(invalidResponse);

        ArticleAiAnalysis result = articleAiService.analyze("Title", "Body", List.of("tag"), Locale.ENGLISH);

        assertEquals("", result.summary());
        assertTrue(result.bulletPoints().isEmpty());
        assertEquals("", result.mainCategory());
        assertTrue(result.secondaryCategories().isEmpty());
    }
}
