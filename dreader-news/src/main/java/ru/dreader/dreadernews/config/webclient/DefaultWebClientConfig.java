package ru.dreader.dreadernews.config.webclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DefaultWebClientConfig extends BaseWebClientConfig {

    @Bean
    public WebClient defaultWebClient(WebClientProperties props) {
        var cfg = cfg(props, "default-client");

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(createHttpClient(cfg)))
                .filter(retryFilter(cfg))
                .build();
    }
}
