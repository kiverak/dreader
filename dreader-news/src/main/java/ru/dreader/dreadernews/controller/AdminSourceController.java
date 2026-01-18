package ru.dreader.dreadernews.controller;

import dto.SourceDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreadernews.service.SourceService;

@RestController
@RequestMapping("/api/admin/source")
@RequiredArgsConstructor
public class AdminSourceController {

    private final SourceService sourceService;

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
