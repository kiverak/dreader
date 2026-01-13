package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.ArticleResponse;
import ru.dreader.dreadernews.entity.Article;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.Tag;
import ru.dreader.dreadernews.enums.PostStatus;
import ru.dreader.dreadernews.service.TagService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArticlePostMapper {

    private final TagService tagService;

    public Post map(Article article, ArticleResponse response) {
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
    private String buildText(Article article, ArticleResponse response) {
        StringBuilder textBuilder = new StringBuilder();

        // Title as bold link
        textBuilder.append("<a href=\"").append(article.getUrl()).append("\">")
                .append("<b>").append(response.title()).append("</b>")
                .append("</a>").append("\n\n");

        textBuilder.append(response.summary()).append("\n\n");

        int maxCounter = 10;
        if (response.summaryBullets() != null) {
            for (String bullet : response.summaryBullets()) {
                if (maxCounter > 10) break;
                textBuilder.append("- ").append(bullet).append("\n");
                maxCounter++;
            }
        }

        return textBuilder.toString();
    }
}
