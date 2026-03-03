package ru.dreader.dreadernews.config.webclient;

import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Configuration
public class ThreadsConfig extends BaseWebClientConfig {

    @Bean
    @Profile("local")
    public WebClient threadsWebClientLocal(WebClientProperties props) {
        var cfg = cfg(props, "threads-client");

        HttpClient httpClient = createHttpClient(cfg);

        httpClient = httpClient.proxy(proxy -> proxy
                .type(ProxyProvider.Proxy.HTTP)
                .host("127.0.0.1")
                .port(2080)
        );

        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("https://graph.threads.net/v1.0")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(retryFilter(cfg))
                .build();
    }

    @Bean
    @Profile("!local")
    public WebClient threadsWebClient(WebClientProperties props) {
        var cfg = cfg(props, "threads-client");

        HttpClient httpClient = createHttpClient(cfg);

        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("https://graph.threads.net/v1.0")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(retryFilter(cfg))
                .build();
    }
}
