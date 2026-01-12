package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.PostDto;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.mapper.PostMapper;
import ru.dreader.dreadernews.repo.PostRepository;

@Log4j2
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final TagService tagService;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public PostDto getById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        return postMapper.toDto(post);
    }


    public void save(Post post) {
        postRepository.save(post);
    }
}
