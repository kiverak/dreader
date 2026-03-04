package ru.dreader.dreadernews.dto;

public record ThreadsShortLivedTokenResponse(
        String access_token,
        Long user_id
) {
}
