package ru.dreader.dreadernews.dto;

import ru.dreader.dreadernews.enums.PostStatus;

import java.util.List;

public record PostDto(
        String text,
        PostStatus status,
        List<String> mediaUrls,
        List<Long> categoryIds
        ) {
}
