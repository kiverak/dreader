package ru.dreader.dreadernews.publisher;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelRateLimiter {

    // Telegram рекомендует: до 30 запросов/сек на бота и до 1/сек на чат
    private static final int GLOBAL_TOKENS_PER_SECOND = 30;
    private static final int CHAT_TOKENS_PER_SECOND = 1;

    private final Bucket globalBucket = new Bucket(GLOBAL_TOKENS_PER_SECOND);
    private final ConcurrentHashMap<String, Bucket> chatBuckets = new ConcurrentHashMap<>();

    public void acquire(String chatId) {
        globalBucket.acquire();
        chatBuckets
                .computeIfAbsent(chatId, id -> new Bucket(CHAT_TOKENS_PER_SECOND))
                .acquire();
    }

    private static class Bucket {
        private final int capacity;
        private double tokens;
        private long lastRefillNanos;

        Bucket(int capacity) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized void acquire() {
            refill();
            while (tokens < 1.0) {
                long sleepNanos = (long) (1_000_000_000.0 / capacity);
                try {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                } catch (InterruptedException ignored) {}
                refill();
            }
            tokens -= 1.0;
        }

        private void refill() {
            long now = System.nanoTime();
            double seconds = (now - lastRefillNanos) / 1_000_000_000.0;
            if (seconds <= 0) {
                return;
            }
            double newTokens = seconds * capacity;
            tokens = Math.min(capacity, tokens + newTokens);
            lastRefillNanos = now;
        }
    }
}
