package ru.dreader.dreadernews.exceptions;

public class ThreadsRateLimitedException extends RuntimeException {

    private final int retryAfterSeconds;

    public ThreadsRateLimitedException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
