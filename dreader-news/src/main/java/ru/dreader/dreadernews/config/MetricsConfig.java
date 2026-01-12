package ru.dreader.dreadernews.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter tasksProcessed(MeterRegistry registry) {
        return Counter.builder("llm_tasks_processed_total")
                .description("Количество успешно обработанных статей")
                .register(registry);
    }

    @Bean
    public Counter tasksFailed(MeterRegistry registry) {
        return Counter.builder("llm_tasks_failed_total")
                .description("Количество ошибок при обработке статей")
                .register(registry);
    }

    @Bean
    public Timer taskDuration(MeterRegistry registry) {
        return Timer.builder("llm_task_duration_seconds")
                .description("Время обработки одной статьи")
                .publishPercentileHistogram()
                .register(registry);
    }
}
