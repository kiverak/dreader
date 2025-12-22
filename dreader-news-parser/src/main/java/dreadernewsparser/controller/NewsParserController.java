package dreadernewsparser.controller;

import dreadernewsparser.dto.NewsArticle;
import dreadernewsparser.service.ArticleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/news")
public class NewsParserController {
    private final int NEWS_BATCH_SIZE = 10;

    private final ArticleService articleService;

    @GetMapping("/getNews")
    public List<NewsArticle> getNews() {
        return articleService.getNewArticles(NEWS_BATCH_SIZE);
    }
}
