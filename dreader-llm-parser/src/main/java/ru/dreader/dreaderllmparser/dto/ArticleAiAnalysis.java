package ru.dreader.dreaderllmparser.dto;

import java.util.List;

public record ArticleAiAnalysis(
        String summary,
        List<String> bulletPoints,
        String mainCategory,
        List<String> secondaryCategories
) {}
