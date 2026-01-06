package ru.dreader.dreaderusers.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KeycloakWebClientConfig {

    @Value("${keycloak.realm-url}")
    private String realmUrl;

    @Bean
    public WebClient keycloakWebClient(OAuth2AuthorizedClientManager manager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);

        oauth.setDefaultClientRegistrationId("keycloak");

        return WebClient.builder()
                .apply(oauth.oauth2Configuration())
                .baseUrl(realmUrl)
                .build();
    }
}
