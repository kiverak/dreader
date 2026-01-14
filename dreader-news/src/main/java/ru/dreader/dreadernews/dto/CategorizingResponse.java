package ru.dreader.dreadernews.dto;

import java.util.List;

public record CategorizingResponse(
        List<CategorizingArticleResult> results
) {}
