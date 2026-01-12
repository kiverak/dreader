package ru.dreader.dreaderllmparser.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreader.dreaderllmparser.dto.ArticleResponse;
import ru.dreader.dreaderllmparser.dto.ParseRequest;
import ru.dreader.dreaderllmparser.service.ArticleProcessingService;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArticleController {

    private final ArticleProcessingService processingService;

    // Глобальная блокировка
    private final ReentrantLock lock = new ReentrantLock(true);

    // Метрики Prometheus
    private final Timer queueWaitTimer;
    private final Timer lockHoldTimer;
    private final Counter lockAcquireFailures;

    @PostMapping("/parse")
    public ArticleResponse parse(@RequestBody ParseRequest request) {

        long startWait = System.nanoTime();
        boolean acquired = false;

        try {
            // Пытаемся получить lock в течение 30 секунд
            acquired = lock.tryLock(30, TimeUnit.SECONDS);

            if (!acquired) {
                lockAcquireFailures.increment();
                throw new RuntimeException("Timeout: unable to acquire parse lock within 30 seconds");
            }

            // Сколько времени запрос ждал lock
            long waitNanos = System.nanoTime() - startWait;
            queueWaitTimer.record(waitNanos, TimeUnit.NANOSECONDS);

            // Измеряем время выполнения обработки
            return lockHoldTimer.record(() -> {
                String language = "ru";
                String region = "RU";

                return processingService.processHtml(
                        request.url(),
                        request.title(),
                        request.body(),
                        request.rawTags(),
                        new Locale.Builder()
                                .setLanguage(language)
                                .setRegion(region)
                                .build()
                );
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for parse lock", e);

        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }
}

