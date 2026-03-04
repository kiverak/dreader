package ru.dreader.dreadernews.dto;

public record ThreadsCodeShortLivedTokenRequest(
        String code,
        Long channelId
) {
}
