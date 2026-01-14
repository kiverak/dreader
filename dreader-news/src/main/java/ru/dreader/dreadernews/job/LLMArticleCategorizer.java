package ru.dreader.dreadernews.job;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.dreader.dreadernews.dto.CategorizingResponse;
import ru.dreader.dreadernews.dto.Pair;
import ru.dreader.dreadernews.entity.Article;
import ru.dreader.dreadernews.service.RatingCalculator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class LLMArticleCategorizer {

    private final JdbcTemplate jdbcTemplate;
    private final LLMArticleProcessor processor;
    private final RatingCalculator ratingCalculator;

    // Prometheus metrics
    private final Counter tasksProcessed;
    private final Counter tasksFailed;
    private final Timer taskDuration;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    // any stable key
    private static final long WORKER_LOCK_KEY = 321654987L;

    @PostConstruct
    public void startWorker() {
        if (!tryAcquireClusterLock()) {
            log.info("LLM worker: another instance already holds the cluster lock, worker will not start");
            return;
        }

        worker.submit(this::workerLoop);
        log.info("Articles LLM Categorizing Worker started");
    }

    private boolean tryAcquireClusterLock() {
        Boolean locked = jdbcTemplate.queryForObject(
                "SELECT pg_try_advisory_lock(?)", Boolean.class, WORKER_LOCK_KEY);
        return Boolean.TRUE.equals(locked);
    }

    private void releaseClusterLock() {
        try {
            jdbcTemplate.update("SELECT pg_advisory_unlock(?)", WORKER_LOCK_KEY);
        } catch (Exception e) {
            log.warn("Failed to release advisory lock", e);
        }
    }

    private void workerLoop() {
        while (running) {
            try {
                Timer.Sample sample = Timer.start();
                boolean processed = processArticlesBunch();
                if (processed) {
                    tasksProcessed.increment();
                }
                sample.stop(taskDuration);
            } catch (Exception e) {
                tasksFailed.increment();
                log.error("Unexpected error in LLM parsing worker", e);
                try {
                    Thread.sleep(2000); // backoff
                } catch (InterruptedException ignored) {
                }
            }
        }
        log.info("Articles LLM Parsing Worker stopped gracefully");
    }

    /**
     * @return true if article was processed, false if no work was done
     */
    protected boolean processArticlesBunch() {
        Pair<CategorizingResponse, List<Article>> categorizingResponse = processor.categorizeArticleBunch();
        if (categorizingResponse == null) return false;

        List<Long> articlesToPublishIds = ratingCalculator.getHighRatedArticlesToPublish(categorizingResponse);

        processor.deleteArticlesWithLowRatingAndDuplicates(categorizingResponse.second(), articlesToPublishIds);
        processor.createProcessedArticlesToPublish(articlesToPublishIds);

        return true;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Stopping Articles LLM Categorizing Worker...");
        running = false;
        worker.shutdown();
        try {
            if (!worker.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Worker did not stop in time, forcing shutdown");
                worker.shutdownNow();
            }
        } catch (InterruptedException e) {
            worker.shutdownNow();
        } finally {
            releaseClusterLock();
        }
    }

}
