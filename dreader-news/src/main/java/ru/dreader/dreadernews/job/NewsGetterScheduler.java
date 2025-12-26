package ru.dreader.dreadernews.job;

import org.springframework.scheduling.annotation.Scheduled;
import ru.dreader.dreadernews.dto.NewsArticle;
import org.springframework.stereotype.Service;
import ru.dreader.dreadernews.dto.SourceDetails;
import ru.dreader.dreadernews.service.SourceService;
import ru.dreader.dreadernews.web.NewsParserClient;
import ru.dreader.dreadernews.service.ArticleService;

import java.util.List;

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

    @Scheduled(fixedRate = 60_000) // каждые 60 секунд
    public void getNews() {
        System.out.println("Getting sources...");
        List<SourceDetails> allSources = newsParserClient.getAllSources();
        sourceService.saveAll(allSources);

        List<NewsArticle> news = newsParserClient.getNews();
        articleService.saveAll(news);
    }

}
