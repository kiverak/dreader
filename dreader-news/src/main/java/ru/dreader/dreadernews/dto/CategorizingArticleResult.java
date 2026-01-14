package ru.dreader.dreadernews.dto;

import java.util.List;

public record CategorizingArticleResult(
        Long id,
        List<Long> matchedCategoryIds,
        List<Long> duplicateIds
) {}
