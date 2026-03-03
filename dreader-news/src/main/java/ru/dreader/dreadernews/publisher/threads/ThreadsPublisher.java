package ru.dreader.dreadernews.publisher.threads;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.dreadernews.publisher.ChannelRateLimiter;
import ru.dreader.dreadernews.publisher.Publisher;
import ru.dreader.dreadernews.service.ThreadsTokenService;

import java.time.Instant;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class ThreadsPublisher implements Publisher {

    private static final int MAX_ATTEMPTS = 3;

    private final WebClient threadsWebClient;
    private final ChannelRateLimiter rateLimiter;
    private final ThreadsTokenService tokenService;

    @Override
    public Platform getPlatform() {
        return Platform.THREADS;
    }

    @Override
    public PublishResult publish(Post post, Channel channel) {
        String accessToken = tokenService.getValidToken(channel);
        String clientId = channel.getCredentials().get("clientId");

        if (accessToken == null || clientId == null) {
            return PublishResult.builder()
                    .success(false)
                    .errorMessage("Missing accessToken or clientId in channel credentials")
                    .channel(channel)
                    .post(post)
                    .build();
        }

        PublishResult result = sendWithRetry(post, accessToken, clientId);
        result.setChannel(channel);
        result.setPost(post);

        return result;
    }

    private PublishResult sendWithRetry(Post post, String accessToken, String clientId) {
        int attempt = 0;

        while (attempt < MAX_ATTEMPTS) {
            attempt++;

            try {
                rateLimiter.acquire(clientId);
                return send(post, accessToken, clientId);

            } catch (ThreadsRateLimitedException e) {
                int retryAfter = e.retryAfterSeconds();
                log.warn("Threads rate limited. Retry after {} seconds", retryAfter);
                sleepSeconds(retryAfter > 0 ? retryAfter : 1);

            } catch (TransientThreadsException e) {
                int backoff = backoffSeconds(attempt);
                log.warn("Transient Threads error. Backoff {} seconds", backoff);
                sleepSeconds(backoff);

            } catch (Exception e) {
                log.error("Non-retryable Threads error", e);
                return PublishResult.builder()
                        .success(false)
                        .errorMessage("Threads error (non-retryable): " + e.getMessage())
                        .build();
            }
        }

        return PublishResult.builder()
                .success(false)
                .errorMessage("Threads error: failed after " + MAX_ATTEMPTS + " attempts")
                .build();
    }

    private PublishResult send(Post post, String accessToken, String clientId) {
        // TODO sendSinglePhoto, sendMediaGroup
        return sendText(post, accessToken, clientId);
    }

    private PublishResult sendText(Post post, String accessToken, String clientId) {
        String url = "/" + clientId + "/threads";

        ThreadsResponse response = executeThreadsRequest(url, post, accessToken);

        if (Boolean.TRUE.equals(response.success())) {
            return PublishResult.builder()
                    .success(true)
                    .publishedAt(Instant.now())
                    .externalId(response.id()) // Threads returns post ID
                    .build();
        }

        throw buildExceptionFromResponse(response);
    }

    private ThreadsResponse executeThreadsRequest(String url, Post post, String token) {
        try {
            return threadsWebClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(Map.of("text", post.getText()))
                    .retrieve()
                    .bodyToMono(ThreadsResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Threads HTTP error: " + e.getStatusCode().value() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Threads I/O error: " + e.getMessage(), e);
        }
    }

    private RuntimeException buildExceptionFromResponse(ThreadsResponse response) {
        int code = response.error_code() != null ? response.error_code() : 0;
        String desc = response.error() != null ? response.error() : "Unknown Threads error";

        if (code == 429) {
            int retryAfter = response.retry_after() != null ? response.retry_after() : 0;
            return new ThreadsRateLimitedException(desc, retryAfter);
        }

        if (code >= 500 && code <= 504) {
            return new TransientThreadsException(desc);
        }

        return new RuntimeException("Threads error " + code + ": " + desc);
    }

    private void sleepSeconds(int seconds) {
        if (seconds <= 0) return;
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
        }
    }

    private int backoffSeconds(int attempt) {
        return (int) Math.pow(2, attempt - 1);
    }

    // --- Custom exceptions ---

    public static class ThreadsRateLimitedException extends RuntimeException {

        private final int retryAfterSeconds;

        public ThreadsRateLimitedException(String message, int retryAfterSeconds) {
            super(message);
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public int retryAfterSeconds() {
            return retryAfterSeconds;
        }
    }


    public static class TransientThreadsException extends RuntimeException {
        public TransientThreadsException(String message) {
            super(message);
        }
    }
}
