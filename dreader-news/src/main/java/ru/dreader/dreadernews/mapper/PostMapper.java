package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.PostDto;
import ru.dreader.dreadernews.entity.Category;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PostMedia;
import ru.dreader.dreadernews.repo.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final CategoryRepository categoryRepository;

    public Post toEntity(PostDto postDto) {
        if (postDto == null) {
            return null;
        }

        Post post = new Post();
        post.setText(postDto.text());
        post.setStatus(postDto.status());
//        post.setMedia(postDto.mediaUrls());

        List<Category> categories = categoryRepository.findAllById(postDto.categoryIds());
        post.setCategories(categories);

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

        List<String> mediaUrls = post.getMedia().stream()
                .map(PostMedia::getUrl)
                .toList();

        List<Long> categoryIds = post.getCategories().stream().map(Category::getId).toList();

        return new PostDto(
                post.getText(),
                post.getStatus(),
                mediaUrls,
                categoryIds,
                post.getUpdatedAt()
        );
    }
}
