package ru.dreader.dreadernews.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleDto(
        String url,
        String title,
        Integer viewsCount,
        Integer commentsCount,
        String content,
        String shortContent,
        String imageUrl,
        LocalDateTime publicationDate,
        List<String> tags
) {}