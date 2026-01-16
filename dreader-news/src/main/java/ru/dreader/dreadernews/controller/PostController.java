package ru.dreader.dreadernews.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreadernews.dto.PostDto;
import ru.dreader.dreadernews.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/public/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/{id}")
    public PostDto getById(@PathVariable Long id) {
        return postService.getById(id);
    }

    @GetMapping
    public List<PostDto> getPublished(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String order
    ) {
        return postService.getPublished(categoryId, size, page, sort, order);
    }

}
