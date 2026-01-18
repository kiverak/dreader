package ru.dreader.dreadernews.controller;

import dto.SourceDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreadernews.service.SourceService;

import java.util.List;

@RestController
@RequestMapping("/api/public/source")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    @GetMapping("/{id}")
    public SourceDetails getById(@PathVariable Long id) {
        return sourceService.getById(id);
    }

    @GetMapping
    public List<SourceDetails> getAll() {
        return sourceService.getAll();
    }

}
