package ru.dreader.dreaderusers.auth;

import io.netty.channel.ChannelOption;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Logout Handler
 */
@Log4j2
@Component
public class KeycloakLogoutHandler implements LogoutHandler {

    private final WebClient webClient;

    public KeycloakLogoutHandler() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .responseTimeout(Duration.ofSeconds(2));

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof OidcUser)) {
            return;
        }

        logoutFromKeycloak((OidcUser) auth.getPrincipal());
    }

    private void logoutFromKeycloak(OidcUser user) {
        String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";

        webClient.get().uri(uriBuilder -> uriBuilder
                        .path(endSessionEndpoint)
                        .queryParam("id_token_hint", user.getIdToken().getTokenValue())
                        .build())
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(resp -> {
                    log.info("Successfully logged out from Keycloak, email: {}", user.getUserInfo().getEmail());
                }).doOnError(err -> {
                    log.error("Failed to propagate logout to Keycloak, email: {}", user.getUserInfo().getEmail(), err);
                })
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal -> log.warn("Retrying logout for {}, attempt {}", user.getUserInfo().getEmail(), retrySignal.totalRetries() + 1)))
                .onErrorResume(e -> Mono.empty())
                .block(); // logout — терминальная операция, блокировка допустима
    }
}
