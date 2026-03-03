package ru.dreader.dreadernews.dto;

public record ThreadsShortLivedTokenRequest(
        String token,
        Long channelId
) {
}
