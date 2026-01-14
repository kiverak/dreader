package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.ArticleResponse;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.ProcessedArticle;
import ru.dreader.dreadernews.entity.Tag;
import ru.dreader.dreadernews.enums.PostStatus;
import ru.dreader.dreadernews.service.TagService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcessedArticlePostMapper {

    private final TagService tagService;

    public Post map(ProcessedArticle article, ArticleResponse response) {
        Post post = new Post();

        post.setText(buildText(article, response));
        post.setStatus(PostStatus.PENDING);
        post.setScheduledAt(Instant.now()); // TODO logic for scheduling

        // Map tags
        List<String> tagNames = new ArrayList<>();
        if (response.mainCategory() != null) {
            tagNames.add(response.mainCategory());
        }
        if (response.secondaryCategories() != null) {
            tagNames.addAll(response.secondaryCategories());
        }

        List<Tag> tags = tagService.getOrCreateByNames(tagNames);
        post.setTags(new HashSet<>(tags));

        return post;
    }

    // Construct text from title, summary and bullets
    private String buildText(ProcessedArticle article, ArticleResponse response) {
        StringBuilder textBuilder = new StringBuilder();

        // Title as bold link
        textBuilder.append("<a href=\"").append(article.getUrl()).append("\">")
                .append("<b>").append(article.getSource().getName()).append("</b>")
                .append("</a>")
                .append("<b>").append(": ").append(response.summary()).append("</b>").append("\n\n");

        if (response.summaryBullets() != null) {
            for (String bullet : response.summaryBullets()) {
                textBuilder.append("– ").append(bullet).append("\n");
            }
        }

        return textBuilder.toString();
    }
}
