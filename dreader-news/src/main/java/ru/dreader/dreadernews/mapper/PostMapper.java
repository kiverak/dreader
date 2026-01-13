package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.PostDto;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PostMedia;
import ru.dreader.dreadernews.entity.Tag;
import ru.dreader.dreadernews.service.SourceService;
import ru.dreader.dreadernews.service.TagService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final TagService tagService;
    private final SourceService sourceService;

    public Post toEntity(PostDto postDto) {
        if (postDto == null) {
            return null;
        }

        Post post = new Post();
        post.setText(postDto.text());
        post.setStatus(postDto.status());
        post.setTags(new HashSet<>(tagService.getOrCreateByNames(postDto.tags())));
//        post.setMedia(postDto.mediaUrls());

        return post;
    }

    public List<Post> toEntity(List<PostDto> postDtoList) {
        List<Post> posts = new ArrayList<>();

        for (PostDto dto : postDtoList) {
            Post post = toEntity(dto);
            posts.add(post);
        }
        return posts;
    }

    public PostDto toDto(Post post) {
        if (post == null) {
            return null;
        }

        List<String> tagNames = post.getTags() != null ?
                post.getTags().stream().map(Tag::getName).toList() :
                Collections.emptyList();
        List<String> mediaUrls = post.getMedia().stream()
                .map(PostMedia::getUrl)
                .toList();

        return new PostDto(
                post.getText(),
                post.getStatus(),
                tagNames,
                mediaUrls
        );
    }
}
