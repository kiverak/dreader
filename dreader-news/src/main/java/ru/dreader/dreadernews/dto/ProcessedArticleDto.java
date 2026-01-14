package ru.dreader.dreadernews.dto;

import java.time.Instant;
import java.util.List;

public record ProcessedArticleDto(
        Long id,
        String title,
        String content,
        String shortContent,
        String url,
        String imageUrl,
        Long sourceId,
        String sourceName,
        Instant publicationDate,
        List<String> tags,
        boolean llmParsed,
        int rate
) {
}
