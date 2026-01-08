package ru.dreader.dreadernews.job;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import ru.dreader.dreadernews.dto.ArticleDto;
import org.springframework.stereotype.Service;
import ru.dreader.dreadernews.dto.SourceDetails;
import ru.dreader.dreadernews.service.SourceService;
import ru.dreader.dreadernews.web.NewsParserClient;
import ru.dreader.dreadernews.service.ArticleService;

import java.util.List;

// TODO remove after Kafka sending creation
@Log4j2
@Service
public class NewsGetterScheduler {

    private final NewsParserClient newsParserClient;
    private final ArticleService articleService;
    private final SourceService sourceService;

    public NewsGetterScheduler(NewsParserClient newsParserClient, ArticleService articleService, SourceService sourceService) {
        this.newsParserClient = newsParserClient;
        this.articleService = articleService;
        this.sourceService = sourceService;
    }

    @Scheduled(fixedRate = 5_000) // каждые 5 секунд
    public void getNews() {
        List<SourceDetails> allSources = newsParserClient.getAllSources();
        sourceService.saveOrUpdateAll(allSources);

        log.info("Getting news...");
        List<ArticleDto> news = newsParserClient.getNews();
        articleService.saveAll(news);
        log.info("{} fresh news saved", news.size());
    }

}
