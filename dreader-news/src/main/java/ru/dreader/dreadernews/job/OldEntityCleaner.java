package ru.dreader.dreadernews.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.service.PostService;
import ru.dreader.dreadernews.service.ProcessedArticleService;

import java.time.Instant;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class OldEntityCleaner {

    private final PostService postService;
    private final ProcessedArticleService processedArticleService;

//    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    @Scheduled(cron = "0 0 3 * * *") // every day at 03:00
    @SchedulerLock(name = "cleanOldEntities", lockAtMostFor = "20m", lockAtLeastFor = "1m")
    public void cleanOldEntities() {
        log.info("Entity cleaning scheduler started...");
        Instant now = Instant.now();

        cleanOldUnpublishedPosts(now);
        cleanOldProcessedArticles(now);

        log.info("Entity cleaning scheduler finished");
    }

    private void cleanOldUnpublishedPosts(Instant now) {
        log.info("Cleaning unpublished posts started...");

        try {
            List<Long> ids = postService.getOldUnpublishedIds(now);

            if (ids.isEmpty()) {
                log.info("No unpublished posts to clean");
                return;
            }

            postService.delete(ids);
            log.info("Old unpublished posts cleaned: {}", ids.size());
        } catch (Exception e) {
            log.error("Failed to clean unpublished posts", e);
        }
    }

    private void cleanOldProcessedArticles(Instant now) {
        log.info("Cleaning processed articles started...");

        try {
            List<Long> ids = processedArticleService.getOldProcessedArticleIds(now);

            if (ids.isEmpty()) {
                log.info("No processed articles to clean");
                return;
            }

            processedArticleService.delete(ids);
            log.info("Old processed articles cleaned: {}", ids.size());
        } catch (Exception e) {
            log.error("Failed to clean processed articles", e);
        }
    }
}
