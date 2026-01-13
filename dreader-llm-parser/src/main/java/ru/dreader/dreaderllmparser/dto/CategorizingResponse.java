package ru.dreader.dreaderllmparser.dto;

import java.util.List;

public record CategorizingResponse(
        List<CategorizingArticleResult> results
) {}
