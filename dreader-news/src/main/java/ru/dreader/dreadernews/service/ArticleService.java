package ru.dreader.dreadernews.service;

import ru.dreader.dreadernews.dto.NewsArticle;
import ru.dreader.dreadernews.entity.Article;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dreader.dreadernews.mapper.ArticleMapper;import ru.dreader.dreadernews.repo.ArticleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    @Transactional
    public void saveAll(List<NewsArticle> newsArticles) {
        List<Article> articles = articleMapper.toEntity(newsArticles);
        articleRepository.saveAll(articles);
    }

}
