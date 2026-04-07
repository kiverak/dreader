package ru.dreader.dreadernews.dto;

import ru.dreader.dreadernews.enums.PostStatus;

import java.time.Instant;
import java.util.List;

public record PostDto(
        String text,
        String summary,
        String url,
        String sourceName,
        PostStatus status,
        List<String> mediaUrls,
        List<Long> categoryIds,
        Instant createdAt
        ) {
}
