package ru.dreader.dreadernews.dto;

// Для retry_after и прочих параметров
public record TelegramResponseParameters(Integer retry_after) {
}
