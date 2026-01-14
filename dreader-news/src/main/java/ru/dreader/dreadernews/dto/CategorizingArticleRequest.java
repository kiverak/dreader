package ru.dreader.dreadernews.dto;

import java.util.List;

public record CategorizingArticleRequest(
        Long id,
        String title,
        List<String> tags
) {}
