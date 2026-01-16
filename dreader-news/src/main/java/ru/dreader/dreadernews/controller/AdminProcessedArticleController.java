package ru.dreader.dreadernews.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreadernews.dto.ChannelDto;
import ru.dreader.dreadernews.dto.ProcessedArticleDto;
import ru.dreader.dreadernews.service.ProcessedArticleService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/processedArticles")
@RequiredArgsConstructor
public class AdminProcessedArticleController {

    private final ProcessedArticleService processedArticleService;

    @GetMapping("/{id}")
    public ProcessedArticleDto getById(@PathVariable Long id) {
        return processedArticleService.getById(id);
    }

    @GetMapping
    public List<ProcessedArticleDto> getLast(
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order
    ) {
        return processedArticleService.getProcessedArticles(tagId, size, page, sort, order);
    }


    @PostMapping
    public ProcessedArticleDto create(@RequestBody ProcessedArticleDto processedArticleDto) {
        return processedArticleService.create(processedArticleDto);
    }

    @PutMapping("/{id}")
    public ProcessedArticleDto update(@PathVariable Long id, @RequestBody ProcessedArticleDto processedArticleDto) {
        return processedArticleService.update(id, processedArticleDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        processedArticleService.delete(id);
    }

}
