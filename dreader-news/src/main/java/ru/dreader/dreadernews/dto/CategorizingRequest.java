package ru.dreader.dreadernews.dto;

import java.util.List;

public record CategorizingRequest(
        List<CategorizingArticleRequest> articles,
        List<CategorizingCategoryRequest> categories
) {}
