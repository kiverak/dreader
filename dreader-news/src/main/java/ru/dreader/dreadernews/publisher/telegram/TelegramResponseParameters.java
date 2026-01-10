package ru.dreader.dreadernews.publisher.telegram;

// Для retry_after и прочих параметров
public record TelegramResponseParameters(Integer retry_after) {
}
