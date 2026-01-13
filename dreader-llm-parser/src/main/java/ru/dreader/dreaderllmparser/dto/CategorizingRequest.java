package ru.dreader.dreaderllmparser.dto;

import java.util.List;

public record CategorizingRequest(
        List<CategorizingArticleRequest> articles,
        List<CategorizingCategoryRequest> categories
) {}
