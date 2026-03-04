package ru.dreader.dreadernews.dto;

public record ThreadsCreateContainerRequest(
        String text,
        String media_type,
        String access_token) {
}
