package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.PostDto;
import ru.dreader.dreadernews.entity.Category;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PostMedia;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostMapper {

    public PostDto toDto(Post post) {
        if (post == null) {
            return null;
        }

        List<String> mediaUrls = post.getMedia().stream()
                .map(PostMedia::getUrl)
                .toList();

        List<Long> categoryIds = post.getCategories().stream().map(Category::getId).toList();

        return new PostDto(
                post.getText(),
                post.getSummary(),
                post.getUrl(),
                post.getSourceName(),
                post.getStatus(),
                mediaUrls,
                categoryIds,
                post.getCreatedAt()
        );
    }
}
