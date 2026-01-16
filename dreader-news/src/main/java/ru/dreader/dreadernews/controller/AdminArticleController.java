package ru.dreader.dreadernews.controller;

import dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreadernews.service.ArticleService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @GetMapping("/{id}")
    public ArticleDto getById(@PathVariable Long id) {
        return articleService.getById(id);
    }

    @GetMapping
    public List<ArticleDto> getLast(
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order
    ) {
        return articleService.getLast(tagId, size, page, sort, order);
    }

}
