package ru.dreader.dreadernews.publisher.threads;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import ru.dreader.dreadernews.dto.ThreadsCreateContainerResponse;
import ru.dreader.dreadernews.dto.ThreadsPostResponse;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.entity.ThreadsToken;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.dreadernews.exceptions.ThreadsRateLimitedException;
import ru.dreader.dreadernews.exceptions.TransientThreadsException;
import ru.dreader.dreadernews.publisher.ChannelRateLimiter;
import ru.dreader.dreadernews.publisher.Publisher;
import ru.dreader.dreadernews.service.ThreadsTokenService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class ThreadsPublisher implements Publisher {

    private static final int MAX_ATTEMPTS = 3;

    private final RestClient threadsRestClient;
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

        String text = post.getText().substring(0, 500); // TODO remake text building

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("text", text);
        formData.add("media_type", mediaType);
        formData.add("access_token", accessToken.getAccessToken());

        return threadsRestClient.post()
                .uri("/v1.0/{user_id}/threads", accessToken.getUserId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(status -> status.value() == 429, (req, resp) -> handle429(resp))
                .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> handle4xx(resp))
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> handle5xx(resp))
                .onStatus(HttpStatusCode::is1xxInformational, (req, resp) -> handle1xx(resp))
                .body(ThreadsCreateContainerResponse.class);
    }

    private ThreadsPostResponse publishPost(ThreadsCreateContainerResponse container, ThreadsToken accessToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("creation_id", container.id());
        formData.add("access_token", accessToken.getAccessToken());

        return threadsRestClient.post()
                .uri("/v1.0/{user_id}/threads_publish", accessToken.getUserId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                // --- RATE LIMIT: HTTP 429 ---
                .onStatus(status -> status.value() == 429, (req, resp) -> handle429(resp))
                // --- RATE LIMIT: ERROR CODE 613 ---
                .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> handlePublish4xx(resp))
                // --- TRANSIENT ERRORS: 5xx ---
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> handlePublish5xx(resp))
                .onStatus(HttpStatusCode::is1xxInformational, (req, resp) -> handlePublish1xx(resp))
                .body(ThreadsPostResponse.class);
    }

    private void handle429(ClientHttpResponse response) {
        String retryAfterHeader = response.getHeaders().getFirst("Retry-After");
        String body = getResponseBody(response);

        if (retryAfterHeader != null) {
            int retryAfter = Integer.parseInt(retryAfterHeader);
            log.warn("Threads rate limited (429). Retry after {} seconds. Body: {}", retryAfter, body);
            throw new ThreadsRateLimitedException("Rate limited", retryAfter);
        } else {
            log.warn("Threads rate limited (429, no Retry-After). Body: {}", body);
            throw new ThreadsRateLimitedException("Rate limited", 1);
        }
    }

    private void handle4xx(ClientHttpResponse response) {
        String body = getResponseBody(response);

        if (body.contains("\"code\":613")) {
            log.warn("Threads rate limited (code 613). Body: {}", body);
            throw new ThreadsRateLimitedException("Rate limited (613)", 1);
        }

        log.error("Threads 4xx error while creating container: {}", body);
        throw new RuntimeException("Threads 4xx error: " + body);
    }

    private void handle5xx(ClientHttpResponse response) {
        String body = getResponseBody(response);
        log.error("Threads 5xx error while creating container: {}", body);
        throw new TransientThreadsException("Threads 5xx error: " + body);
    }

    private void handle1xx(ClientHttpResponse response) {
        String body = getResponseBody(response);
        log.error("Threads 1xx error while creating container: {}", body);
        throw new TransientThreadsException("Threads 1xx error: " + body);
    }

    private String getResponseBody(ClientHttpResponse response) {
        try {
            return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Failed to read error response body", e);
            return "[unable to read body]";
        }
    }

    private void handlePublish4xx(ClientHttpResponse response) {
        String body = getResponseBody(response);

        if (body.contains("\"code\":613")) {
            log.warn("Threads rate limited (code 613). Body: {}", body);
            throw new ThreadsRateLimitedException("Rate limited (613)", 1);
        }

        log.error("Threads 4xx error while publishing post: {}", body);
        throw new RuntimeException("Threads 4xx error: " + body);
    }

    private void handlePublish5xx(ClientHttpResponse response) {
        String body = getResponseBody(response);
        log.error("Threads 5xx error while publishing post: {}", body);
        throw new TransientThreadsException("Threads 5xx error: " + body);
    }

    private void handlePublish1xx(ClientHttpResponse response) {
        String body = getResponseBody(response);
        log.error("Threads 1xx error while publishing post: {}", body);
        throw new TransientThreadsException("Threads 1xx error: " + body);
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
}
