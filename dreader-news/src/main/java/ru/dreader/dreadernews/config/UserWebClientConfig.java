package ru.dreader.dreadernews.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/*
WebClient для фронтового доступа
работает с OAuth2 Login
умеет обновлять токены
умеет брать токен из AuthorizedClientRepository
умеет fallback‑ить к SecurityContext
 */
@Configuration
public class UserWebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient userWebClient(OAuth2AuthorizedClientManager manager) {
        var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);   // берёт токен из SecurityContext
        oauth.setDefaultOAuth2AuthorizedClient(true); // <-- ключевой момент

        return WebClient.builder()
                .apply(oauth.oauth2Configuration())
                .build();
    }

}
