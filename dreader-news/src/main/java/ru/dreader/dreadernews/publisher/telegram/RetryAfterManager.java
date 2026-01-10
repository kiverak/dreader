package ru.dreader.dreadernews.publisher.telegram;

import org.springframework.stereotype.Component;

@Component
public class RetryAfterManager {

    public void handleRetryAfter(Throwable error) {
        String msg = error.getMessage();
        if (msg == null) return;

        if (msg.contains("retry after")) {
            int seconds = extractRetryAfter(msg);
            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException ignored) {}
        }
    }

    private int extractRetryAfter(String msg) {
        try {
            String[] parts = msg.split("retry after");
            return Integer.parseInt(parts[1].trim().split("[^0-9]")[0]);
        } catch (Exception e) {
            return 1;
        }
    }
}

