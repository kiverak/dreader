package ru.dreader.dreadernews.publisher.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PostMedia;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.dreadernews.publisher.Publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TelegramPublisher implements Publisher {

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final int MAX_ATTEMPTS = 3;

    private final WebClient telegramClient;
    private final TelegramRateLimiter rateLimiter;

    @Override
    public Platform getPlatform() {
        return Platform.TELEGRAM;
    }

    @Override
    public PublishResult publish(Post post, Channel channel) {
        String botToken = channel.getCredentials().get("botToken");
        String chatId = channel.getCredentials().get("chatId");

        if (botToken == null || chatId == null) {
            return PublishResult.builder()
                    .success(false)
                    .errorMessage("Missing botToken or chatId in channel credentials")
                    .build();
        }

        return sendWithRetry(post, botToken, chatId);
    }

    private PublishResult sendWithRetry(Post post, String botToken, String chatId) {
        int attempt = 0;

        while (attempt < MAX_ATTEMPTS) {
            attempt++;
            try {
                rateLimiter.acquire(chatId);
                return send(post, botToken, chatId);
            } catch (TelegramRateLimitedException e) {
                int retryAfter = e.retryAfterSeconds();
                sleepSeconds(retryAfter > 0 ? retryAfter : 1);
            } catch (TransientTelegramException e) {
                sleepSeconds(backoffSeconds(attempt));
            } catch (Exception e) {
                return PublishResult.builder()
                        .success(false)
                        .errorMessage("Telegram error (non-retryable): " + e.getMessage())
                        .build();
            }
        }

        return PublishResult.builder()
                .success(false)
                .errorMessage("Telegram error: failed after " + MAX_ATTEMPTS + " attempts")
                .build();
    }

    private PublishResult send(Post post, String botToken, String chatId) {
        List<PostMedia> media = post.getMedia();

        if (media == null || media.isEmpty()) {
            return sendText(post, botToken, chatId);
        }

        if (media.size() == 1) {
            return sendSinglePhoto(post, media.get(0), botToken, chatId);
        }

        return sendMediaGroup(post, media, botToken, chatId);
    }

    private PublishResult sendText(Post post, String botToken, String chatId) {
        String url = TELEGRAM_API_BASE + botToken + "/sendMessage";

        Map<String, Object> payload = Map.of(
                "chat_id", chatId,
                "text", post.getText(),
                "parse_mode", "HTML"
        );

        TelegramResponse response = executeTelegramRequest(url, payload);

        if (response.ok()) {
            return PublishResult.builder()
                    .success(true)
                    .externalId(response.result() != null
                            ? String.valueOf(response.result().messageId())
                            : null)
                    .build();
        }

        throw buildExceptionFromResponse(response);
    }

    private PublishResult sendSinglePhoto(Post post, PostMedia media, String botToken, String chatId) {
        String url = TELEGRAM_API_BASE + botToken + "/sendPhoto";

        Map<String, Object> payload = Map.of(
                "chat_id", chatId,
                "photo", media.getUrl(),
                "caption", post.getText(),
                "parse_mode", "HTML"
        );

        TelegramResponse response = executeTelegramRequest(url, payload);

        if (response.ok()) {
            return PublishResult.builder()
                    .success(true)
                    .externalId(response.result() != null
                            ? String.valueOf(response.result().messageId())
                            : null)
                    .build();
        }

        throw buildExceptionFromResponse(response);
    }

    private PublishResult sendMediaGroup(Post post, List<PostMedia> media, String botToken, String chatId) {
        String url = TELEGRAM_API_BASE + botToken + "/sendMediaGroup";

        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < media.size(); i++) {
            PostMedia m = media.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("type", "photo");
            item.put("media", m.getUrl());
            if (i == 0 && post.getText() != null && !post.getText().isBlank()) {
                item.put("caption", post.getText());
                item.put("parse_mode", "HTML");
            }
            items.add(item);
        }

        Map<String, Object> payload = Map.of(
                "chat_id", chatId,
                "media", items
        );

        TelegramResponse response = executeTelegramRequest(url, payload);

        if (response.ok()) {
            // sendMediaGroup возвращает массив сообщений; тут можно расширить DTO,
            // пока оставим null либо первое message_id при доработке.
            return PublishResult.builder()
                    .success(true)
                    .externalId(null)
                    .build();
        }

        throw buildExceptionFromResponse(response);
    }

    private TelegramResponse executeTelegramRequest(String url, Object payload) {
        try {
            return telegramClient.post()
                    .uri(url)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(TelegramResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            // HTTP-статус != 2xx
            throw new RuntimeException("Telegram HTTP error: " + e.getStatusCode().value() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Telegram I/O error: " + e.getMessage(), e);
        }
    }

    private RuntimeException buildExceptionFromResponse(TelegramResponse response) {
        int code = response.error_code() != null ? response.error_code() : 0;
        String desc = response.description() != null ? response.description() : "Unknown Telegram error";

        if (code == 429) {
            int retryAfter = response.parameters() != null && response.parameters().retry_after() != null
                    ? response.parameters().retry_after()
                    : 0;
            return new TelegramRateLimitedException(desc, retryAfter);
        }

        if (code >= 500 && code <= 504) {
            return new TransientTelegramException(desc);
        }

        return new RuntimeException("Telegram error " + code + ": " + desc);
    }

    private void sleepSeconds(int seconds) {
        if (seconds <= 0) return;
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
        }
    }

    private int backoffSeconds(int attempt) {
        // простой экспоненциальный backoff: 1, 2, 4
        return (int) Math.pow(2, attempt - 1);
    }

    // Специальные исключения под разный тип ошибок

    private static class TelegramRateLimitedException extends RuntimeException {
        private final int retryAfterSeconds;

        TelegramRateLimitedException(String message, int retryAfterSeconds) {
            super(message);
            this.retryAfterSeconds = retryAfterSeconds;
        }

        int retryAfterSeconds() {
            return retryAfterSeconds;
        }
    }

    private static class TransientTelegramException extends RuntimeException {
        TransientTelegramException(String message) {
            super(message);
        }
    }
}
