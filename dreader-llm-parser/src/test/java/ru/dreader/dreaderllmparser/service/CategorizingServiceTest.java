package ru.dreader.dreaderllmparser.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import ru.dreader.dreaderllmparser.dto.*;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategorizingServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private CategorizingService categorizingService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        categorizingService = new CategorizingService(chatClientBuilder);
    }

    @Test
    void rankArticles_ValidResponse_ReturnsResponse() {
        String jsonResponse = """
                [
                  {
                    "id": 1,
                    "matchedCategoryIds": [2, 5],
                    "duplicateIds": [7]
                  }
                ]
                """;

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(jsonResponse);

        CategorizingRequest request = new CategorizingRequest(
                List.of(new CategorizingArticleRequest(1L, "Title", List.of("tag"))),
                List.of(new CategorizingCategoryRequest(2L, "Cat"))
        );

        CategorizingResponse result = categorizingService.rankArticles(request, Locale.ENGLISH);

        assertEquals(1, result.results().size());
        CategorizingArticleResult item = result.results().get(0);
        assertEquals(1L, item.id());
        assertEquals(2, item.matchedCategoryIds().size());
        assertEquals(1, item.duplicateIds().size());
    }

    @Test
    void rankArticles_InvalidJson_ReturnsEmptyResponse() {
        String invalidResponse = "Not JSON";

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(invalidResponse);

        CategorizingRequest request = new CategorizingRequest(
                List.of(new CategorizingArticleRequest(1L, "Title", List.of("tag"))),
                List.of(new CategorizingCategoryRequest(2L, "Cat"))
        );

        CategorizingResponse result = categorizingService.rankArticles(request, Locale.ENGLISH);

        assertTrue(result.results().isEmpty());
    }
}
