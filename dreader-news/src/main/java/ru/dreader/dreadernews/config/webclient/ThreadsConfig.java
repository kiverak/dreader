package ru.dreader.dreadernews.config.webclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
public class ThreadsConfig extends BaseWebClientConfig {

    @Bean(name = "threadsWebClient")
    @Qualifier("threadsWebClient")
    public WebClient threadsWebClient(WebClientProperties props) {
        var cfg = cfg(props, "threads-client");

        HttpClient httpClient = createHttpClient(cfg);

        return WebClient.builder()
                .baseUrl("https://graph.threads.net")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(retryFilter(cfg))
                .build();
    }

    @Bean(name = "threadsRestClient")
    @Qualifier("threadsRestClient")
    public RestClient threadsRestClient(WebClientProperties props) {
        var cfg = cfg(props, "threads-client");

        ReactorClientHttpRequestFactory requestFactory = createRequestFactory(cfg);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("https://graph.threads.net")
                .requestInterceptor(retryInterceptor(cfg))
                .build();
    }
}
