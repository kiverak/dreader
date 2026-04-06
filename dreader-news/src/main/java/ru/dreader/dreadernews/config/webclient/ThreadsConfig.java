package ru.dreader.dreadernews.config.webclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Slf4j
@Configuration
public class ThreadsConfig extends BaseWebClientConfig {

    @Bean(name = "threadsWebClient")
    @Profile("local")
    @Qualifier("threadsWebClient")
    public WebClient threadsWebClientLocal(WebClientProperties props) {
        var cfg = cfg(props, "threads-client");

        HttpClient httpClient = createHttpClient(cfg);

        httpClient = httpClient.proxy(proxy -> proxy
                .type(ProxyProvider.Proxy.HTTP)
                .host("127.0.0.1")
                .port(2080)
        );

        return WebClient.builder()
                .baseUrl("https://graph.threads.net")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(retryFilter(cfg))
                .build();
    }

    @Bean(name = "threadsWebClient")
    @Profile("!local")
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
    @Profile("local")
    @Qualifier("threadsRestClient")
    public RestClient threadsRestClientLocal(WebClientProperties props) {
        var cfg = cfg(props, "threads-client");

        HttpClient httpClient = createHttpClient(cfg)
                .proxy(proxy -> proxy
                        .type(ProxyProvider.Proxy.HTTP)
                        .host("127.0.0.1")
                        .port(2080));

        ReactorClientHttpRequestFactory requestFactory = new ReactorClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("https://graph.threads.net")
                .requestInterceptor(retryInterceptor(cfg))
                .build();
    }

    @Bean(name = "threadsRestClient")
    @Profile("!local")
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
