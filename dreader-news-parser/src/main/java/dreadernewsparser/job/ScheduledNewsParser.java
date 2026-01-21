package dreadernewsparser.job;

import dreadernewsparser.entity.Source;
import dreadernewsparser.parser.ParserService;
import dreadernewsparser.service.ArticleProducer;
import dreadernewsparser.service.SourceService;
import dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScheduledNewsParser {

    private final ParserService parserService;
    private final SourceService sourceService;
    private final ArticleProducer articleProducer;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Scheduled(fixedRate = 3_600_000) // 1 hour
    public void parseSites() {
        List<Source> sources = sourceService.findAll();
        if (sources.isEmpty()) {
            log.info("No sources to parse.");
            return;
        }

        log.info("Parsing cycle started. Sources: {}", sources.size());

        for (Source source : sources) {
            executor.submit(() -> processSource(source));
        }
    }

    private void processSource(Source source) {
        try {
            List<String> urls = parserService.findNewArticles(source);
            for (String url : urls) {
                ArticleDto article;
                try {
                    article = parserService.parse(url, source);
                    if (article.publicationDate().isAfter(Instant.now().minus(1, ChronoUnit.HOURS))) {
                        log.info("The article is too new, skipping: {}, {}", article.sourceName(), article.title());
                        continue;
                    }
                } catch (Exception e) {
                    log.error("Failed to parse {}", url, e);
                    continue;
                }
                articleProducer.sendArticle(article);
            }
        } catch (Exception e) {
            log.error("Failed to process source {}", source.getUrl(), e);
        }
    }
}
