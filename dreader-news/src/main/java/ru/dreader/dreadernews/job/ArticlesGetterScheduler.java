package ru.dreader.dreadernews.job;

import dto.ArticleDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import dto.SourceDetails;
import ru.dreader.dreadernews.service.SourceService;
import ru.dreader.dreadernews.web.NewsParserClient;
import ru.dreader.dreadernews.service.ArticleService;

import java.util.List;

// TODO remove after Kafka sending creation
@Log4j2
@Service
public class ArticlesGetterScheduler {

    private final NewsParserClient newsParserClient;
    private final ArticleService articleService;
    private final SourceService sourceService;

    public ArticlesGetterScheduler(NewsParserClient newsParserClient, ArticleService articleService, SourceService sourceService) {
        this.newsParserClient = newsParserClient;
        this.articleService = articleService;
        this.sourceService = sourceService;
    }

    @Scheduled(fixedRate = 5_000) // каждые 5 секунд
    public void getArticles() {
        List<SourceDetails> allSources = newsParserClient.getAllSources();
        sourceService.saveOrUpdateAll(allSources);

        log.info("Getting articles...");
        List<ArticleDto> articleDTOs = newsParserClient.getNews();
        articleService.saveAll(articleDTOs);
        log.info("{} fresh articles saved", articleDTOs.size());
    }

}
