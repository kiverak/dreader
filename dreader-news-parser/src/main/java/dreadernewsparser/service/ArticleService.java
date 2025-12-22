package dreadernewsparser.service;

import dreadernewsparser.dto.ArticleSourcePair;
import dreadernewsparser.dto.NewsArticle;
import dreadernewsparser.entity.Article;
import dreadernewsparser.mapper.ArticleMapper;
import dreadernewsparser.repo.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    @Transactional
    public int saveAll(List<ArticleSourcePair> batch) {
        List<String> urls = batch.stream()
                .map(ArticleSourcePair::newsArticle)
                .map(NewsArticle::url)
                .toList();

        Set<String> existing = articleRepository.findExistingUrls(urls);

        List<ArticleSourcePair> filtered = batch.stream()
                .filter(e -> !existing.contains(e.newsArticle().url()))
                .toList();

        List<Article> articles = new ArrayList<>();
        for (ArticleSourcePair pair : filtered) {
            Article article = articleMapper.toEntity(pair.newsArticle(), pair.source());
            articles.add(article);
        }

        return articleRepository.saveAll(articles).size();
    }

    @Transactional
    public List<NewsArticle> getNewArticles(int batchSize) {
        List<Article> articles = articleRepository.findUnpostedWithLimit(batchSize);
        for (Article a : articles) {
            a.setPushed(true);
        }
        return articles.stream()
                .map(articleMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }
}