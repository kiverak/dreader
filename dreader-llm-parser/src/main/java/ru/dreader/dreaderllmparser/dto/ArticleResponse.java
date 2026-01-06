package ru.dreader.dreaderllmparser.dto;

import java.util.List;

public record ArticleResponse(
        String url,
        String title,
        String summary,
        List<String> summaryBullets,
        String mainCategory,
        List<String> secondaryCategories
) {
}
