package ru.dreader.dreadernews.mapper;

import ru.dreader.dreadernews.dto.NewsArticle;
import ru.dreader.dreadernews.entity.Article;
import ru.dreader.dreadernews.entity.Source;
import ru.dreader.dreadernews.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.service.TagService;

import java.util.ArrayList;import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ArticleMapper {

    private final TagService tagService;

    public Article toEntity(NewsArticle newsArticle) {
        if (newsArticle == null) {
            return null;
        }

        Article article = new Article();
        article.setTitle(newsArticle.title());
        article.setViewsCount(newsArticle.viewsCount());
        article.setCommentsCount(newsArticle.commentsCount());
        article.setContent(newsArticle.content());
        if (newsArticle.shortContent() == null || newsArticle.shortContent().isEmpty()) {
            article.setShortContent(newsArticle.content().substring(0, Math.min(200, newsArticle.content().length())));
        } else {
            article.setShortContent(newsArticle.shortContent());
        }
        article.setUrl(newsArticle.url());
        article.setImageUrl(newsArticle.imageUrl());
        article.setPublicationDate(newsArticle.publicationDate());
        article.setTags(tagService.getOrCreateByNames(newsArticle.tags()));

        return article;
    }

    public Article toEntity(NewsArticle newsArticle, Source source) {
        Article article = toEntity(newsArticle);
        article.setSource(source);
        if (article.getTags() == null || article.getTags().isEmpty()) {
            article.setTags(source.getDefaultTags());
        }
        return article;
    }

    public List<Article> toEntity(List<NewsArticle> newsArticles) {
        List<Article> articles = new ArrayList<>();
        for (NewsArticle na : newsArticles) {
            articles.add(toEntity(na));
        }
        return articles;
    }

    public NewsArticle toDto(Article article) {
        if (article == null) {
            return null;
        }

        List<String> tagNames = article.getTags() != null ?
                article.getTags().stream().map(Tag::getName).toList() :
                Collections.emptyList();

        return new NewsArticle(
                article.getUrl(),
                article.getTitle(),
                article.getViewsCount(),
                article.getCommentsCount(),
                article.getContent(),
                article.getShortContent(),
                article.getImageUrl(),
                article.getPublicationDate(),
                tagNames
        );
    }
}