package ru.dreader.dreadernews.config.webclient;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
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
public class UserWebClientConfig extends BaseWebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient userWebClient(
            OAuth2AuthorizedClientManager manager,
            WebClientProperties props
    ) {
        var cfg = cfg(props, "user-client");

        var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth.setDefaultOAuth2AuthorizedClient(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(createHttpClient(cfg)))
                .apply(oauth.oauth2Configuration())
                .filter(retryFilter(cfg))
                .build();
    }
}
