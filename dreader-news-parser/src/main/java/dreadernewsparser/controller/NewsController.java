package dreadernewsparser.controller;

import dreadernewsparser.dto.NewsArticle;
import dreadernewsparser.service.ArticleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// TODO remove after Kafka sending creation
@RestController
@AllArgsConstructor
@RequestMapping("/api/news")
public class NewsController {
    private final int NEWS_BATCH_SIZE = 10;

    private final ArticleService articleService;

    @GetMapping()
    public List<NewsArticle> getNewsBatch() {
        return articleService.getNewArticles(NEWS_BATCH_SIZE);
    }
}
