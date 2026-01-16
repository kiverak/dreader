package ru.dreader.dreadernews.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import dto.SourceDetails;
import ru.dreader.dreadernews.service.SourceService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/source")
@RequiredArgsConstructor
public class AdminSourceController {

    private final SourceService sourceService;

    @GetMapping("/{id}")
    public SourceDetails getById(@PathVariable Long id) {
        return sourceService.getById(id);
    }

    @GetMapping
    public List<SourceDetails> getAll() {
        return sourceService.getAll();
    }

    @PostMapping()
    public SourceDetails create(@RequestBody SourceDetails sourceDetails) {
        return sourceService.create(sourceDetails);
    }

    @PutMapping("/{id}")
    public SourceDetails update(@PathVariable Long id, @RequestBody SourceDetails sourceDetails) {
        return sourceService.update(id, sourceDetails);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        sourceService.delete(id);
    }

}
