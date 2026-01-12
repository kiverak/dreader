package ru.dreader.dreadernews.dto;

import java.util.List;

/*
Response of LLM parser
Similar as in dreader-llm-parser service
 */
public record ArticleResponse(
        String url,
        String title,
        String summary,
        List<String> summaryBullets,
        String mainCategory,
        List<String> secondaryCategories
) {
}
