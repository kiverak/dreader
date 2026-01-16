package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.PostDto;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.mapper.PostMapper;
import ru.dreader.dreadernews.repo.PostRepository;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public PostDto getById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        return postMapper.toDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostDto> getPublished(Long categoryId, Integer size, Integer page, String sort, String order) {
        Sort sortBy = Sort.by(Sort.Direction.fromString(order != null ? order : "DESC"), sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);

        Page<Post> postPage;
        if (categoryId != null) {
            postPage = postRepository.findByCategories_Id(categoryId, pageable);
        } else {
            postPage = postRepository.findAll(pageable);
        }

        return postPage.getContent().stream()
                .map(postMapper::toDto)
                .toList();
    }

    @Transactional
    public void save(Post post) {
        postRepository.save(post);
    }

    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }
}
