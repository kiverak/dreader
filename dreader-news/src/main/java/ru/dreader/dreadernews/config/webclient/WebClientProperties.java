package ru.dreader.dreadernews.config.webclient;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "app.webclient")
public class WebClientProperties {

    private final Map<String, ClientConfig> client = new HashMap<>();

    @Data
    public static class ClientConfig {
        private int connectTimeout;
        private int readTimeout;
        private RetryConfig retry;
    }

    @Data
    public static class RetryConfig {
        private int attempts;
        private long delay;
    }
}
