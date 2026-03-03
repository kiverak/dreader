package ru.dreader.dreadernews.dto;

public record ThreadsLongLivedTokenResponse(
        String access_token,
        Long token_type,
        Long expires_in
) {
}
