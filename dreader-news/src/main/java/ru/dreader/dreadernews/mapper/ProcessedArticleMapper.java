package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.ProcessedArticleDto;
import ru.dreader.dreadernews.entity.Article;
import ru.dreader.dreadernews.entity.ProcessedArticle;
import ru.dreader.dreadernews.entity.Source;
import ru.dreader.dreadernews.entity.Tag;
import ru.dreader.dreadernews.service.RatingCalculator;
import ru.dreader.dreadernews.service.SourceService;
import ru.dreader.dreadernews.service.TagService;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class ProcessedArticleMapper {

    private final TagService tagService;
    private final SourceService sourceService;
    private final RatingCalculator ratingCalculator;

    public ProcessedArticle map(Article article) {
        ProcessedArticle processedArticle = new ProcessedArticle();
        processedArticle.setTitle(article.getTitle());
        processedArticle.setContent(article.getContent());
        processedArticle.setShortContent(article.getShortContent());
        processedArticle.setUrl(article.getUrl());
        processedArticle.setImageUrl(article.getImageUrl());
        processedArticle.setPublicationDate(article.getPublicationDate());
        processedArticle.setLlmParsed(false);
        processedArticle.setRate(ratingCalculator.rating(article));

        try {
            processedArticle.setTags(article.getTags());
        } catch (Exception e) {
            log.info("Lazy tags loading for article id {}", article.getId());
            processedArticle.setTags(tagService.getTagsByArticleId(article.getId()));
        }

        try {
            processedArticle.setSource(article.getSource());
        } catch (Exception e) {
            log.info("Lazy source loading for article id {}", article.getId());
            processedArticle.setSource(sourceService.getByArticleId(article.getId()));
        }

        return processedArticle;
    }

    public ProcessedArticle toEntity(ProcessedArticleDto dto) {
        ProcessedArticle entity = new ProcessedArticle();
        fillOutEntity(entity, dto);

        return entity;
    }

    public void updateEntity(ProcessedArticle entity, ProcessedArticleDto dto) {
        fillOutEntity(entity, dto);
    }

    private void fillOutEntity(ProcessedArticle entity, ProcessedArticleDto dto) {
        entity.setTitle(dto.title());
        entity.setContent(dto.content());
        entity.setShortContent(dto.shortContent());
        entity.setUrl(dto.url());
        entity.setImageUrl(dto.imageUrl());
        entity.setPublicationDate(dto.publicationDate());
        entity.setLlmParsed(dto.llmParsed());
        entity.setRate(dto.rate());

        if (dto.tags() != null && !dto.tags().isEmpty()) {
            entity.setTags(tagService.getOrCreateByNames(dto.tags()));
        } else {
            entity.setTags(null);
        }

        if (dto.sourceId() != null) {
            entity.setSource(sourceService.getBySourceById(dto.sourceId()));
        } else {
            entity.setSource(null);
        }
    }

    public ProcessedArticleDto toDto(ProcessedArticle processedArticle) {
        List<Tag> tags;
        try {
            tags = processedArticle.getTags();
        } catch (Exception e) {
            log.info("Lazy tags loading for processed article id {}", processedArticle.getId());
            tags = tagService.getTagsByProcessedArticleId(processedArticle.getId());
        }

        Source source;
        try {
            source = processedArticle.getSource();
        } catch (Exception e) {
            log.info("Lazy source loading for processed article id {}", processedArticle.getId());
            source = sourceService.getByProcessedArticleId(processedArticle.getId());
        }

        return new ProcessedArticleDto(
                processedArticle.getId(),
                processedArticle.getTitle(),
                processedArticle.getContent(),
                processedArticle.getShortContent(),
                processedArticle.getUrl(),
                processedArticle.getImageUrl(),
                source != null ? source.getId() : null,
                source != null ? source.getName() : null,
                processedArticle.getPublicationDate(),
                tags.stream().map(Tag::getName).toList(),
                processedArticle.isLlmParsed(),
                processedArticle.getRate()
        );
    }
}
