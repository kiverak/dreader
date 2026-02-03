package ru.dreader.dreadernews.config.webclient;

import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TelegramConfig extends BaseWebClientConfig {

    @Bean
    public WebClient telegramClient(WebClientProperties props) {
        var cfg = cfg(props, "telegram-client");

        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(createHttpClient(cfg)))
                .filter(retryFilter(cfg))
                .build();
    }
}
