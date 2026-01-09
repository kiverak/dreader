package dreadernewsparser.job;

import dreadernewsparser.dto.ArticleSourcePair;
import dreadernewsparser.dto.ArticleDto;
import dreadernewsparser.entity.Source;
import dreadernewsparser.parser.ParserService;
import dreadernewsparser.service.ArticleService;
import dreadernewsparser.service.SourceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class ScheduledNewsParser {

    // Batch параметры
    final int BATCH_SIZE = 20;
    final long BATCH_TIMEOUT_MS = 2000;

    private static final Logger log = LoggerFactory.getLogger(ScheduledNewsParser.class);
    private final ParserService parserService;
    private final SourceService sourceService;
    private final ArticleService articleService;

    // Специальный объект-маркер ("отравленная пилюля") для сигнала о завершении работы
    private static final ArticleSourcePair POISON_PILL = new ArticleSourcePair(null, null);

    @Scheduled(fixedRate = 3_600_000) // каждый час
    public void parseSites() {
        List<Source> sources = sourceService.findAll();
        if (sources.isEmpty()) {
            log.info("No sources to parse.");
            return;
        }

        BlockingQueue<ArticleSourcePair> articlesQueue = new LinkedBlockingQueue<>(100);
        CountDownLatch producersLatch = new CountDownLatch(sources.size());

        // Consumer, который пишет посты в бд
        Thread consumerThread = new Thread(() -> {
            List<ArticleSourcePair> batch = new ArrayList<>(BATCH_SIZE);
            long lastFlushTime = System.currentTimeMillis();

            try {
                while (true) {
                    ArticleSourcePair pair = articlesQueue.poll(500, TimeUnit.MILLISECONDS);

                    long now = System.currentTimeMillis();

                    if (pair == POISON_PILL) {
                        flushBatch(batch);
                        log.info("Consumer received poison pill. Stopping.");
                        break;
                    }

                    if (pair != null) {
                        batch.add(pair);
                    }

                    boolean batchFull = batch.size() >= BATCH_SIZE;
                    boolean timeoutReached = (now - lastFlushTime) >= BATCH_TIMEOUT_MS;

                    if (batchFull || timeoutReached) {
                        flushBatch(batch);
                        lastFlushTime = now;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Consumer interrupted", e);
            }
        }, "news-consumer");

        consumerThread.start();

        // Producers — виртуальные потоки, парсеры
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        for (Source source : sources) {
            executor.submit(() -> {
                try {
                    List<String> urls = parserService.findNewArticles(source);
                    for (String url : urls) {
                        try {
                            ArticleDto article = parserService.parse(url, source);
                            // Ключевой момент: producer может ждать
                            articlesQueue.put(new ArticleSourcePair(article, source));
                            log.info("Queued: {} from {}", article.title(), source.getName());
                        } catch (Exception e) {
                            log.error("Failed to parse {}", url, e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to process source {}", source.getUrl(), e);
                } finally {
                    producersLatch.countDown();
                }
            });
        }

        // Поток, который отправляет сигнал на завершение
        Thread poisonPillSender = new Thread(() -> {
            try {
                producersLatch.await();
                articlesQueue.put(POISON_PILL);
                log.info("Poison pill sent.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "poison-pill-sender");
        poisonPillSender.start();

        executor.shutdown();
        log.info("Parsing cycle started.");
    }

    // TODO: отправлять в Kafka
    private void flushBatch(List<ArticleSourcePair> batch) {
        if (batch.isEmpty()) return;

        try {
            int size = articleService.saveAll(batch);
            batch.clear();
            log.info("Flushed batch of {} articles", size);
        } catch (Exception e) {
            log.error("Failed to flush batch", e);
        }
    }
}
