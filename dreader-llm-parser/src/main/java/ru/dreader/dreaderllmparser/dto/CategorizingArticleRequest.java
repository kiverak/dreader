package ru.dreader.dreaderllmparser.dto;

import java.util.List;

public record CategorizingArticleRequest(
        Long id,
        String title,
        List<String> tags
) {}
