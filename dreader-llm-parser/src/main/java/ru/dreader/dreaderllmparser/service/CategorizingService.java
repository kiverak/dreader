package ru.dreader.dreaderllmparser.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import ru.dreader.dreaderllmparser.dto.*;
import ru.dreader.dreaderllmparser.utils.LLMJsonCleaner;

import java.util.List;

@Log4j2
@Service
public class CategorizingService {

    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public CategorizingService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public CategorizingResponse rankArticles(CategorizingRequest request) {

        String prompt = buildPrompt(request.articles(), request.categories());

        String response = chatClient.prompt(prompt)
                .call()
                .content();

        return parseResponse(response);
    }

    public String buildPrompt(List<CategorizingArticleRequest> articles, List<CategorizingCategoryRequest> categories) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                You are an expert news classifier and deduplication engine.
                For each article, do two tasks:
                
                1. Determine which categories it belongs to based on title and tags.
                   IMPORTANT: categories have id and name. Use the name for classification,
                   but return ONLY category ids.
                
                2. Detect duplicates: articles that describe the same topic or event.
                
                Return STRICT JSON:
                [
                  {
                    "id": <articleId>,
                    "matchedCategoryIds": [1, 2],
                    "duplicateIds": [5, 7]
                  }
                ]
                
                Categories (id → name):
                """);

        for (CategorizingCategoryRequest c : categories) {
            sb.append("- ").append(c.id()).append(": ").append(c.name()).append("\n");
        }

        sb.append("\nArticles:\n");
        for (CategorizingArticleRequest a : articles) {
            sb.append("ID: ").append(a.id()).append("\n");
            sb.append("Title: ").append(a.title()).append("\n");
            sb.append("Tags: ").append(String.join(", ", a.tags())).append("\n\n");
        }

        return sb.toString();
    }

    private CategorizingResponse parseResponse(String json) {
        try {
            // LLMJsonCleaner.parse(json) → чистит мусор, оставляет только JSON‑массив
            JsonNode cleaned = LLMJsonCleaner.parse(json);
            // Преобразуем в список ArticleResult
            List<CategorizingArticleResult> results = mapper.readValue(cleaned.toString(), new TypeReference<>() {
            });
            return new CategorizingResponse(results);
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", e.getMessage());
            return new CategorizingResponse(List.of());
        }
    }

}
