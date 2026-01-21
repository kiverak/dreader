package dreadernewsparser.service;

import dreadernewsparser.dto.ArticleSourcePair;
import dreadernewsparser.entity.Article;
import dreadernewsparser.mapper.ArticleMapper;
import dreadernewsparser.repo.ArticleRepository;
import dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    @Transactional
    public int saveAll(List<ArticleSourcePair> batch) {
        List<String> urls = batch.stream()
                .map(ArticleSourcePair::articleDto)
                .map(ArticleDto::url)
                .toList();

        Set<String> existing = articleRepository.findExistingUrls(urls);

        List<ArticleSourcePair> filtered = batch.stream()
                .filter(e -> !existing.contains(e.articleDto().url()))
                .toList();

        List<Article> articles = new ArrayList<>();
        for (ArticleSourcePair pair : filtered) {
            Article article = articleMapper.toEntity(pair.articleDto(), pair.source());
            articles.add(article);
        }

        return articleRepository.saveAll(articles).size();
    }

    @Transactional
    public List<ArticleDto> getNewArticles(int batchSize) {
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