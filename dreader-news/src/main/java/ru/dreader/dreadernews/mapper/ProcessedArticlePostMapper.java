package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.ArticleResponse;
import ru.dreader.dreadernews.entity.Category;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.ProcessedArticle;
import ru.dreader.dreadernews.enums.PostStatus;
import ru.dreader.dreadernews.repo.CategoryRepository;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProcessedArticlePostMapper {

    private final CategoryRepository categoryRepository;

    public Post map(ProcessedArticle article, ArticleResponse response) {
        Post post = new Post();

        post.setText(buildText(article, response));
        post.setSummary(response.summary());
        post.setUrl(article.getUrl());
        post.setSourceName(article.getSource().getName());
        post.setStatus(PostStatus.PENDING);
        post.setScheduledAt(Instant.now()); // TODO logic for scheduling

        Optional<Category> category = categoryRepository.findByName(response.mainCategory()); // TODO
        category.ifPresent(value -> post.getCategories().add(value));

        post.setRate(article.getRate());

        return post;
    }

    // Construct text from bullets
    private String buildText(ProcessedArticle article, ArticleResponse response) {
        StringBuilder textBuilder = new StringBuilder();

        if (response.summaryBullets() != null) {
            for (String bullet : response.summaryBullets()) {
                textBuilder.append("– ").append(bullet).append("\n");
            }
        }

        return textBuilder.toString();
    }
}
