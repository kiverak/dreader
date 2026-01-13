package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.ChannelDto;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.mapper.ChannelMapper;
import ru.dreader.dreadernews.repo.ChannelRepository;
import ru.dreader.mvc.exception.ResourceNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMapper channelMapper;

    @Transactional(readOnly = true)
    public List<ChannelDto> getAll() {
        return channelMapper.toDtoList(channelRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Set<Channel> getAllChannelsSet() {
        return new HashSet<>(channelRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ChannelDto getById(Long id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));
        return channelMapper.toDto(channel);
    }

    @Transactional
    public ChannelDto create(ChannelDto channelDto) {
        Channel channel = channelMapper.toEntity(channelDto);
        channel = channelRepository.save(channel);
        return channelMapper.toDto(channel);
    }

    @Transactional
    public ChannelDto update(Long id, ChannelDto channelDto) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));
        channelMapper.updateEntity(channel, channelDto);
        channel = channelRepository.save(channel);
        return channelMapper.toDto(channel);
    }

    @Transactional
    public void delete(Long id) {
        if (!channelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Channel not found with id: " + id);
        }
        channelRepository.deleteById(id);
    }
}
