package ru.dreader.dreadernews.exceptions;

public class TransientTelegramException extends RuntimeException {
    public TransientTelegramException(String message) {
        super(message);
    }
}
