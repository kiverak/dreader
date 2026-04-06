package ru.dreader.dreadernews.config.webclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class BaseWebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(BaseWebClientConfig.class);

    protected HttpClient createHttpClient(WebClientProperties.ClientConfig cfg) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, cfg.getConnectTimeout())
                .responseTimeout(Duration.ofMillis(cfg.getReadTimeout()))
                .doOnConnected(conn ->
                        conn.channel().pipeline().addLast(new ReadTimeoutHandler(cfg.getReadTimeout(), TimeUnit.MILLISECONDS))
                );
    }

    protected ExchangeFilterFunction retryFilter(WebClientProperties.ClientConfig cfg) {
        return (request, next) -> next.exchange(request)
                .retryWhen(Retry.fixedDelay(cfg.getRetry().getAttempts(), Duration.ofMillis(cfg.getRetry().getDelay())));
    }

    protected WebClientProperties.ClientConfig cfg(WebClientProperties props, String name) {
        return props.getClient().get(name);
    }

    protected ReactorClientHttpRequestFactory createRequestFactory(WebClientProperties.ClientConfig cfg) {
        HttpClient httpClient = createHttpClient(cfg);
        return new ReactorClientHttpRequestFactory(httpClient);
    }

    protected ClientHttpRequestInterceptor retryInterceptor(WebClientProperties.ClientConfig cfg) {
        int maxAttempts = cfg.getRetry().getAttempts();
        long delayMs = cfg.getRetry().getDelay();

        return (request, body, execution) -> {
            for (int attempt = 0; attempt <= maxAttempts; attempt++) {
                try {
                    return execution.execute(request, body);
                } catch (Exception ex) {
                    if (attempt == maxAttempts || !isRetryable(ex)) {
                        throw ex;
                    }

                    log.warn("Threads API request failed (attempt {}/{}). Retrying in {} ms...",
                            attempt + 1, maxAttempts + 1, delayMs, ex);

                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
            throw new IllegalStateException("All retry attempts exhausted for Threads API");
        };
    }

    private boolean isRetryable(Exception ex) {
        return ex instanceof IOException ||
                ex instanceof org.springframework.web.client.ResourceAccessException ||
                ex.getCause() instanceof IOException;
    }
}
