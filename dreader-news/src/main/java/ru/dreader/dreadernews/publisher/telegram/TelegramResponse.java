package ru.dreader.dreadernews.publisher.telegram;

public record TelegramResponse(
        boolean ok,
        TelegramMessage result,
        Integer error_code,
        String description,
        TelegramResponseParameters parameters
) {
}
