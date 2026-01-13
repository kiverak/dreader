package ru.dreader.dreadernews.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.dreader.dreadernews.dto.ChannelDto;
import ru.dreader.dreadernews.service.ChannelService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/channel")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    public List<ChannelDto> getAll() {
        return channelService.getAll();
    }

    @GetMapping("/{id}")
    public ChannelDto getById(@PathVariable Long id) {
        return channelService.getById(id);
    }

    @PostMapping
    public ChannelDto create(@RequestBody ChannelDto channelDto) {
        return channelService.create(channelDto);
    }

    @PutMapping("/{id}")
    public ChannelDto update(@PathVariable Long id, @RequestBody ChannelDto channelDto) {
        return channelService.update(id, channelDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        channelService.delete(id);
    }
}
