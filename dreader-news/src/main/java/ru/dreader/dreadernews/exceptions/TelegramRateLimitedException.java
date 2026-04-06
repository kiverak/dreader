package ru.dreader.dreadernews.exceptions;

public class TelegramRateLimitedException extends RuntimeException {
    private final int retryAfterSeconds;

    public TelegramRateLimitedException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
