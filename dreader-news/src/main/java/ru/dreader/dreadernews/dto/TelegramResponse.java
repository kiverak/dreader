package ru.dreader.dreadernews.dto;

public record TelegramResponse(
        boolean ok,
        TelegramMessage result,
        Integer error_code,
        String description,
        TelegramResponseParameters parameters
) {
}
