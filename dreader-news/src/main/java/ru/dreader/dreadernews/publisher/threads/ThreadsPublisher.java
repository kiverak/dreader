package ru.dreader.dreadernews.publisher.threads;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.dreader.dreadernews.dto.ThreadsCreateContainerRequest;
import ru.dreader.dreadernews.dto.ThreadsCreateContainerResponse;
import ru.dreader.dreadernews.dto.ThreadsPostResponse;
import ru.dreader.dreadernews.dto.ThreadsPublishRequest;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.entity.ThreadsToken;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.dreadernews.publisher.ChannelRateLimiter;
import ru.dreader.dreadernews.publisher.Publisher;
import ru.dreader.dreadernews.service.ThreadsTokenService;

import java.time.Instant;

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
        ThreadsToken accessToken = tokenService.getValidToken(channel);
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
        Instant now = Instant.now();
        result.setPublishedAt(now);
        result.setCreatedAt(now);
        result.setUpdatedAt(now);

        return result;
    }

    private PublishResult sendWithRetry(Post post, ThreadsToken accessToken, String clientId) {
        int attempt = 0;

        while (attempt < MAX_ATTEMPTS) {
            attempt++;

            try {
                rateLimiter.acquire(clientId);
                return send(post, accessToken);
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

    private PublishResult send(Post post, ThreadsToken accessToken) {
        // TODO sendSinglePhoto, sendMediaGroup
        return sendText(post, accessToken);
    }

    private PublishResult sendText(Post post, ThreadsToken accessToken) {
        ThreadsCreateContainerResponse container = createContainer(post, accessToken, "TEXT");
        ThreadsPostResponse postResponse = publishPost(container, accessToken);

        if (postResponse.id() != null) {
            return PublishResult.builder()
                    .success(true)
                    .publishedAt(Instant.now())
                    .externalId(postResponse.id()) // Threads returns post ID
                    .build();
        }

        throw new RuntimeException("Failed to publish post" + post.getId() + " to Threads");
    }

    private ThreadsCreateContainerResponse createContainer(Post post, ThreadsToken accessToken, String mediaType) {
        String text = post.getText().substring(0, 500);
        ThreadsCreateContainerRequest createRequest =
                new ThreadsCreateContainerRequest(text, mediaType, accessToken.getAccessToken());

        return threadsWebClient.post()
                .uri("/v1.0/{user_id}/threads", accessToken.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .retrieve()

                // --- RATE LIMIT: HTTP 429 ---
                .onStatus(status -> status.value() == 429, response ->
                        response.headers().asHttpHeaders().containsKey("Retry-After")
                                ? response.bodyToMono(String.class).flatMap(body -> {
                            int retryAfter = Integer.parseInt(
                                    response.headers().asHttpHeaders().getFirst("Retry-After")
                            );
                            log.warn("Threads rate limited (429). Retry after {} seconds. Body: {}", retryAfter, body);
                            return Mono.error(new ThreadsRateLimitedException("Rate limited", retryAfter));
                        })
                                : response.bodyToMono(String.class).flatMap(body -> {
                            log.warn("Threads rate limited (429, no Retry-After). Body: {}", body);
                            return Mono.error(new ThreadsRateLimitedException("Rate limited", 1));
                        })
                )

                // --- RATE LIMIT: ERROR CODE 613 ---
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            if (body.contains("\"code\":613")) {
                                log.warn("Threads rate limited (code 613). Body: {}", body);
                                return Mono.error(new ThreadsRateLimitedException("Rate limited (613)", 1));
                            }
                            log.error("Threads 4xx error while creating container: {}", body);
                            return Mono.error(new RuntimeException("Threads 4xx error: " + body));
                        })
                )

                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("Threads 4xx error while creating container: {}", body);
                            return Mono.error(new RuntimeException("Threads 4xx error: " + body));
                        })
                )

                // --- TRANSIENT ERRORS: 5xx ---
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("Threads 5xx error while creating container: {}", body);
                            return Mono.error(new TransientThreadsException("Threads 5xx error: " + body));
                        })
                )

                .bodyToMono(ThreadsCreateContainerResponse.class)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Threads returned empty container response"));
    }

    private ThreadsPostResponse publishPost(ThreadsCreateContainerResponse container, ThreadsToken accessToken) {
        ThreadsPublishRequest publishRequest =
                new ThreadsPublishRequest(container.id(), accessToken.getAccessToken());

        return threadsWebClient.post()
                .uri("/v1.0/{user_id}/threads_publish", accessToken.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(publishRequest)
                .retrieve()

                // --- RATE LIMIT: HTTP 429 ---
                .onStatus(status -> status.value() == 429, response ->
                        response.headers().asHttpHeaders().containsKey("Retry-After")
                                ? response.bodyToMono(String.class).flatMap(body -> {
                            int retryAfter = Integer.parseInt(
                                    response.headers().asHttpHeaders().getFirst("Retry-After")
                            );
                            log.warn("Threads rate limited (429). Retry after {} seconds. Body: {}", retryAfter, body);
                            return Mono.error(new ThreadsRateLimitedException("Rate limited", retryAfter));
                        })
                                : response.bodyToMono(String.class).flatMap(body -> {
                            log.warn("Threads rate limited (429, no Retry-After). Body: {}", body);
                            return Mono.error(new ThreadsRateLimitedException("Rate limited", 1));
                        })
                )

                // --- RATE LIMIT: ERROR CODE 613 ---
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            if (body.contains("\"code\":613")) {
                                log.warn("Threads rate limited (code 613). Body: {}", body);
                                return Mono.error(new ThreadsRateLimitedException("Rate limited (613)", 1));
                            }
                            log.error("Threads 4xx error while publishing post: {}", body);
                            return Mono.error(new RuntimeException("Threads 4xx error: " + body));
                        })
                )

                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("Threads 4xx error while publishing post: {}", body);
                            return Mono.error(new RuntimeException("Threads 4xx error: " + body));
                        })
                )

                // --- TRANSIENT ERRORS: 5xx ---
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("Threads 5xx error while publishing post: {}", body);
                            return Mono.error(new TransientThreadsException("Threads 5xx error: " + body));
                        })
                )

                .bodyToMono(ThreadsPostResponse.class)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Threads returned empty publish response"));
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
