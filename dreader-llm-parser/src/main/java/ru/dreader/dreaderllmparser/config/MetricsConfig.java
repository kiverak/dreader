package ru.dreader.dreaderllmparser.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Timer queueWaitTimer(MeterRegistry registry) {
        return Timer.builder("parse_queue_wait_time_seconds")
                .description("Время ожидания запроса в очереди перед получением lock")
                .publishPercentileHistogram()
                .register(registry);
    }

    @Bean
    public Timer lockHoldTimer(MeterRegistry registry) {
        return Timer.builder("parse_lock_hold_time_seconds")
                .description("Время выполнения обработки под lock")
                .publishPercentileHistogram()
                .register(registry);
    }

    @Bean
    public Counter lockAcquireFailures(MeterRegistry registry) {
        return Counter.builder("parse_lock_acquire_failures_total")
                .description("Количество запросов, которые не смогли получить lock за таймаут")
                .register(registry);
    }
}
