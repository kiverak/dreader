package ru.dreader.dreadernews.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import ru.dreader.dreadernews.dto.ThreadsCodeShortLivedTokenRequest;
import ru.dreader.dreadernews.dto.ThreadsLongLivedTokenResponse;
import ru.dreader.dreadernews.dto.ThreadsShortLivedTokenResponse;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.ThreadsToken;
import ru.dreader.dreadernews.repo.ChannelRepository;
import ru.dreader.dreadernews.repo.ThreadsTokenRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class ThreadsTokenService {

    @Value("${threads.app-id}")
    private String appId;

    @Value("${threads.app-secret}")
    private String appSecret;

    private final ThreadsTokenRepository repo;
    private final ChannelRepository channelRepository;

    @Qualifier("threadsWebClient")
    private final WebClient threadsWebClient;

    public ThreadsTokenService(ThreadsTokenRepository repo, ChannelRepository channelRepository, WebClient threadsWebClient) {
        this.repo = repo;
        this.channelRepository = channelRepository;
        this.threadsWebClient = threadsWebClient;
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
        entity.setExpiresAt(Instant.now().plusSeconds(longToken.expires_in()));

        repo.save(entity);
    }

    private ThreadsLongLivedTokenResponse exchangeForLongLived(String shortLivedToken) {
        return threadsWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/access_token")
                        .queryParam("grant_type", "th_exchange_token")
                        .queryParam("client_secret", appSecret)
                        .queryParam("access_token", shortLivedToken)
                        .build())
                .retrieve()
                .bodyToMono(ThreadsLongLivedTokenResponse.class)
                .block();
    }

    private ThreadsShortLivedTokenResponse requestShortLivedToken(ThreadsCodeShortLivedTokenRequest request) {
        return threadsWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/access_token")
                        .queryParam("client_id", appId)
                        .queryParam("client_secret", appSecret)
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("redirect_uri", "https://localhost:8080/meta/callback")
                        .queryParam("code", request.code())
                        .build())
                .retrieve()
                .bodyToMono(ThreadsShortLivedTokenResponse.class)
                .block();
    }

    @Transactional
    public synchronized void refreshIfNeeded(Channel channel) {
        ThreadsToken entity = repo.findByChannel(channel).orElse(null);
        if (entity == null) return;

        if (entity.getExpiresAt().isBefore(Instant.now().plus(Duration.ofDays(2)))) {
            ThreadsLongLivedTokenResponse refreshed = refreshLongLived(entity.getAccessToken());

            entity.setAccessToken(refreshed.access_token());
            entity.setExpiresAt(Instant.now().plusSeconds(refreshed.expires_in()));

            repo.save(entity);
            return;
        }

        throw new RuntimeException("Token for channel id" + channel.getId() + " is expired, please refresh with new code");
    }

    private ThreadsLongLivedTokenResponse refreshLongLived(String longLivedToken) {
        return threadsWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/refresh_access_token")
                        .queryParam("grant_type", "th_refresh_token")
                        .queryParam("access_token", longLivedToken)
                        .build())
                .retrieve()
                .bodyToMono(ThreadsLongLivedTokenResponse.class)
                .block();
    }

    @Transactional
    public String getValidToken(Channel channel) {
        return repo.findByChannel(channel)
                .map(ThreadsToken::getAccessToken)
                .orElseThrow(() -> new IllegalStateException("Threads token not initialized for channel id " + channel.getId()));
    }

}
