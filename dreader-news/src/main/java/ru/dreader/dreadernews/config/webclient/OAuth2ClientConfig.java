package ru.dreader.dreadernews.config.webclient;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/*
WebClient, который автоматически получает сервисный токен,
автоматически обновляет его,
автоматически добавляет Authorization: Bearer <token>
 */
@Configuration
@EnableConfigurationProperties(WebClientProperties.class)
public class OAuth2ClientConfig extends BaseWebClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientService clientService
    ) {
        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clientService);
        manager.setAuthorizedClientProvider(provider);

        return manager;
    }

    @Bean
    public WebClient serviceWebClient(
            OAuth2AuthorizedClientManager manager,
            WebClientProperties props
    ) {
        var cfg = cfg(props, "service-client");

        var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth.setDefaultClientRegistrationId("keycloak-service");

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(createHttpClient(cfg)))
                .apply(oauth.oauth2Configuration())
                .filter(retryFilter(cfg))
                .build();
    }
}
