package ru.dreader.dreadernews.config.webclient;

import io.netty.channel.ChannelOption;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

public abstract class BaseWebClientConfig {

    protected HttpClient createHttpClient(WebClientProperties.ClientConfig cfg) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, cfg.getConnectTimeout())
                .responseTimeout(Duration.ofMillis(cfg.getReadTimeout()));
    }

    protected ExchangeFilterFunction retryFilter(WebClientProperties.ClientConfig cfg) {
        return (request, next) -> next.exchange(request)
                .retryWhen(Retry.fixedDelay(cfg.getRetry().getAttempts(), Duration.ofMillis(cfg.getRetry().getDelay())));
    }

    protected WebClientProperties.ClientConfig cfg(WebClientProperties props, String name) {
        return props.getClient().get(name);
    }
}
