package ru.dreader.dreaderllmparser.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import ru.dreader.dreaderllmparser.dto.*;
import ru.dreader.dreaderllmparser.utils.LLMJsonCleaner;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Log4j2
@Service
public class CategorizingService {

    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public CategorizingService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public CategorizingResponse rankArticles(CategorizingRequest request, Locale locale) {
        log.info("LLM categorizing call started");
        String prompt = buildPrompt(request.articles(), request.categories(), locale);

        Instant now = Instant.now();

        String response = chatClient.prompt(prompt)
                .call()
                .content();
        
        Instant then = Instant.now();
        
        log.info("LLM categorizing call duration: {} s", (then.toEpochMilli() - now.toEpochMilli()) / 1000.0);

        return parseResponse(response);
    }

    public String buildPrompt(List<CategorizingArticleRequest> articles, List<CategorizingCategoryRequest> categories, Locale locale) {
        String language = (locale.getLanguage().equals("ru")) ? "Russian" : "English";
        StringBuilder sb = new StringBuilder();

        sb.append("""
                You are llama3.1:8b, an expert system for news classification and duplicate detection.
                All article titles, tags and category names are in %s.
                
                Your tasks for EACH article:
                
                1) Category classification:
                   • Categories have "id" and "name".
                   • Use ONLY the Russian "name" for semantic classification.
                   • In the output return ONLY category ids in "matchedCategoryIds".
                
                2) Duplicate detection:
                   • Articles describing the same event or topic are duplicates.
                   • Use semantic similarity, not exact text matching.
                   • Return ids of duplicate articles in "duplicateIds".
                   • If no duplicates exist, return an empty list.
                
                OUTPUT RULES (VERY IMPORTANT):
                • Output MUST be STRICT JSON.
                • NO explanations, NO comments, NO markdown, NO text before or after JSON.
                • Output MUST be a JSON array of objects.
                • Use ONLY integers for ids.
                
                Example of the required output format:
                [
                  {
                    "id": 1,
                    "matchedCategoryIds": [2, 5],
                    "duplicateIds": [7]
                  }
                ]
                
                Categories (id — name):
                """.formatted(language)
        );

        for (CategorizingCategoryRequest c : categories) {
            sb.append(c.id()).append(" — ").append(c.name()).append("\n");
        }

        sb.append("\nArticles:\n");
        for (CategorizingArticleRequest a : articles) {
            sb.append("ID: ").append(a.id()).append("\n");
            sb.append("Title: ").append(a.title()).append("\n");
            sb.append("Tags: ").append(String.join(", ", a.tags())).append("\n\n");
        }

        sb.append("""
                Return ONLY the JSON array. No additional text.
                """);

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
