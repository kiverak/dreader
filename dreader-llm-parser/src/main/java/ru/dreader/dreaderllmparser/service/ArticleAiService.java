package ru.dreader.dreaderllmparser.service;

import ru.dreader.dreaderllmparser.dto.ArticleAiAnalysis;

import java.util.List;
import java.util.Locale;

public interface ArticleAiService {
    ArticleAiAnalysis analyze(String title, String bodyPlainText, List<String> rawTags, Locale locale);
}
