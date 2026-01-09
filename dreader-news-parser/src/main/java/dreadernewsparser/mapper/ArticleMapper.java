package dreadernewsparser.mapper;

import dreadernewsparser.dto.ArticleDto;
import dreadernewsparser.entity.Article;
import dreadernewsparser.entity.Source;
import dreadernewsparser.entity.Tag;
import dreadernewsparser.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ArticleMapper {

    private final TagService tagService;

    public Article toEntity(ArticleDto articleDto) {
        if (articleDto == null) {
            return null;
        }

        Article article = new Article();
        article.setTitle(articleDto.title());
        article.setViewsCount(articleDto.viewsCount());
        article.setCommentsCount(articleDto.commentsCount());
        article.setContent(articleDto.content());
        if (articleDto.shortContent() == null || articleDto.shortContent().isEmpty()) {
            article.setShortContent(articleDto.content().substring(0, Math.min(200, articleDto.content().length())));
        } else {
            article.setShortContent(articleDto.shortContent());
        }
        article.setUrl(articleDto.url());
        article.setImageUrl(articleDto.imageUrl());
        article.setPublicationDate(articleDto.publicationDate());
        article.setTags(tagService.getOrCreateByNames(articleDto.tags()));

        return article;
    }

    public Article toEntity(ArticleDto articleDto, Source source) {
        Article article = toEntity(articleDto);
        article.setSource(source);
        if (article.getTags() == null || article.getTags().isEmpty()) {
            article.setTags(source.getDefaultTags());
        }
        return article;
    }

    public ArticleDto toDto(Article article) {
        if (article == null) {
            return null;
        }

        List<String> tagNames = article.getTags() != null ? 
                article.getTags().stream().map(Tag::getName).toList() :
                Collections.emptyList();

        return new ArticleDto(
                article.getUrl(),
                article.getTitle(),
                article.getSource().getName(),
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