package ru.dreader.dreadernews.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import ru.dreader.dreadernews.dto.ThreadsCodeShortLivedTokenRequest;
import ru.dreader.dreadernews.dto.ThreadsLongLivedTokenResponse;
import ru.dreader.dreadernews.dto.ThreadsShortLivedTokenResponse;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.ThreadsToken;
import ru.dreader.dreadernews.exceptions.ThreadsRateLimitedException;
import ru.dreader.dreadernews.exceptions.TransientThreadsException;
import ru.dreader.dreadernews.repo.ChannelRepository;
import ru.dreader.dreadernews.repo.ThreadsTokenRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class ThreadsTokenService {

    @Value("${threads.app-id}")
    private String appId;

    @Value("${threads.app-secret}")
    private String appSecret;

    private final ThreadsTokenRepository repo;
    private final ChannelRepository channelRepository;

    @Qualifier("threadsRestClient")
    private final RestClient threadsRestClient;

    public ThreadsTokenService(ThreadsTokenRepository repo, ChannelRepository channelRepository, RestClient threadsRestClient) {
        this.repo = repo;
        this.channelRepository = channelRepository;
        this.threadsRestClient = threadsRestClient;
    }

    @Transactional
    public synchronized void requestAndSaveLongLivedToken(ThreadsCodeShortLivedTokenRequest request) {
        Channel channel = channelRepository.findById(request.channelId()).orElseThrow(() -> new IllegalStateException("Channel not found"));
        ThreadsShortLivedTokenResponse shortToken = requestShortLivedToken(request);
        ThreadsLongLivedTokenResponse longToken = exchangeForLongLived(shortToken.access_token());

        Optional<ThreadsToken> existing = repo.findById(request.channelId());
        ThreadsToken entity;
        if (existing.isPresent()) {
            entity = existing.get();
        } else {
            entity = new ThreadsToken();
            entity.setChannel(channel);
        }

        entity.setAccessToken(longToken.access_token());
        entity.setUserId(shortToken.user_id());
        entity.setExpiresAt(Instant.now().plusSeconds(longToken.expires_in()));

        repo.save(entity);
    }

    private ThreadsLongLivedTokenResponse exchangeForLongLived(String shortLivedToken) {
        return threadsRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/access_token")
                        .queryParam("grant_type", "th_exchange_token")
                        .queryParam("client_secret", appSecret)
                        .queryParam("access_token", shortLivedToken)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxTokenError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxTokenError)
                .body(ThreadsLongLivedTokenResponse.class);
    }

    private ThreadsShortLivedTokenResponse requestShortLivedToken(ThreadsCodeShortLivedTokenRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", appId);
        formData.add("client_secret", appSecret);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", "https://localhost:8080/meta/callback");
        formData.add("code", request.code());

        return threadsRestClient.post()
                .uri("/oauth/access_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxTokenError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxTokenError)
                .body(ThreadsShortLivedTokenResponse.class);
    }

    @Transactional
    public synchronized void refreshIfNeeded(Channel channel) {
        ThreadsToken token = repo.findByChannel(channel).orElse(null);
        if (token == null) return;

        if (token.getExpiresAt().isBefore(Instant.now().plus(Duration.ofDays(60)))) {
            ThreadsLongLivedTokenResponse refreshed = refreshLongLived(token.getAccessToken());

            token.setAccessToken(refreshed.access_token());
            token.setExpiresAt(Instant.now().plusSeconds(refreshed.expires_in()));

            repo.save(token);
            return;
        }

        throw new RuntimeException("Token for channel id" + channel.getId() + " is expired, please refresh with new code");
    }

    private ThreadsLongLivedTokenResponse refreshLongLived(String longLivedToken) {
        return threadsRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/refresh_access_token")
                        .queryParam("grant_type", "th_refresh_token")
                        .queryParam("access_token", longLivedToken)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxTokenError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxTokenError)
                .body(ThreadsLongLivedTokenResponse.class);
    }

    @Transactional
    public ThreadsToken getValidToken(Channel channel) {
        return repo.findByChannel(channel)
                .orElseThrow(() -> new IllegalStateException("Threads token not initialized for channel id " + channel.getId()));
    }

    private void handle4xxTokenError(HttpRequest request, ClientHttpResponse response) {
        String body = getResponseBody(response);
        String errorMsg = "Threads token request failed with 4xx";

        if (body.contains("\"code\":613")) {
            log.warn("Threads rate limited (code 613) during token operation. Body: {}", body);
            throw new ThreadsRateLimitedException("Rate limited (613) during token exchange", 60); // 60 секунд по умолчанию
        }

        log.error("Threads 4xx error during token operation [{} {}]: {}",
                request.getMethod(), request.getURI(), body);

        throw new RuntimeException(errorMsg + ": " + body);
    }

    private void handle5xxTokenError(HttpRequest request, ClientHttpResponse response) {
        String body = getResponseBody(response);
        log.error("Threads 5xx error during token operation [{} {}]: {}",
                request.getMethod(), request.getURI(), body);

        throw new TransientThreadsException("Threads 5xx during token exchange: " + body);
    }

    private String getResponseBody(ClientHttpResponse response) {
        try {
            return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Failed to read error response body from Threads", e);
            return "[unable to read body]";
        }
    }
}
