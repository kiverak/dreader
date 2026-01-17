package ru.dreader.dreadernews.publisher.threads;

public record ThreadsResponse(
        Boolean success,
        String id,
        String error,
        Integer error_code,
        Integer retry_after
) {}
